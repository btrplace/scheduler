/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.actionModel;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;
import btrplace.solver.SolverException;
import btrplace.solver.choco.DurationEvaluators;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceBuilder;
import btrplace.solver.choco.chocoUtil.ChocoUtils;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.channeling.BooleanChanneling;
import choco.cp.solver.constraints.reified.ReifiedFactory;
import choco.cp.solver.variables.integer.BoolVarNot;
import choco.cp.solver.variables.integer.BooleanVarImpl;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.UUID;

/**
 * Model an action that allow a running VM to be relocate elsewhere if necessary.
 * The relocation can be performed through a live-migration or a re-instantiation.
 * The re-instantiation consists in forging a new VM having the same characteristics
 * and launching it on the destination node. Once this new VM has been launched, the
 * original VM is shutted down. Such a relocation method may be faster than a migration-based
 * method while being less aggressive for the network. However, the VM must be able to
 * be cloned from a template.
 * <p/>
 * If the relocation is performed with a live-migration, a {@link MigrateVM} action
 * will be generated. If the relocation is performed through a re-instantiation, a {@link ForgeVM},
 * a {@link BootVM}, and a {@link ShutdownVM} actions are generated.
 * <p/>
 * To relocate the VM using a re-instantiation, the VM must first have an attribute {@code clone}
 * set to {@code true}. The re-instantiation duration is then estimated. If it is shorter than
 * the migration duration, then re-instantiation will be preferred.
 * <p/>
 *
 * @author Fabien Hermenier
 */
public class RelocatableVMModel implements KeepRunningVMModel {

    private Slice cSlice, dSlice;

    private ReconfigurationProblem rp;

    private final UUID vm;

    private IntDomainVar state;

    private IntDomainVar duration;

    private IntDomainVar stay;

    private int reInstantiateDuration;

    private UUID src;

    /**
     * The choosed relocation method. 0 for migration, 1 for relocation.
     */
    private IntDomainVar method;

    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the VM managed by the action
     * @throws SolverException if an error occurred
     */
    public RelocatableVMModel(ReconfigurationProblem rp, UUID e) throws SolverException {
        this.vm = e;
        this.rp = rp;

        src = rp.getSourceModel().getMapping().getVMLocation(e);


        prepareRelocationMethod();

        CPSolver s = rp.getSolver();

        cSlice = new SliceBuilder(rp, e, "relocatable(" + e + ").cSlice")
                .setHoster(rp.getNode(rp.getSourceModel().getMapping().getVMLocation(e)))
                .setEnd(rp.makeDuration("relocatable(" + e + ").cSlice_end"))
                .build();

        dSlice = new SliceBuilder(rp, vm, "relocatable(" + vm + ").dSlice")
                .setStart(rp.makeDuration("relocatable(", vm, ").dSlice_start"))
                .build();

        IntDomainVar move = s.createBooleanVar(rp.makeVarLabel("relocatable(", vm, ").move"));
        s.post(ReifiedFactory.builder(move, s.neq(cSlice.getHoster(), dSlice.getHoster()), s));

        stay = new BoolVarNot(s, rp.makeVarLabel("relocatable(", e, ").stay"), (BooleanVarImpl) move);

        s.post(s.leq(duration, cSlice.getDuration()));
        s.post(s.leq(duration, dSlice.getDuration()));
        s.post(s.eq(cSlice.getEnd(), s.plus(dSlice.getStart(), duration)));

        s.post(s.leq(cSlice.getDuration(), rp.getEnd()));
        s.post(s.leq(dSlice.getDuration(), rp.getEnd()));
        s.post(s.leq(dSlice.getEnd(), rp.getEnd()));

        //If we allow re-instantiate, then the dSlice duration will consume necessarily after the forgeDuration
        s.post(new BooleanChanneling(stay, duration, 0));

        if (!getRelocationMethod().isInstantiated()) {
            //TODO: not very compliant with the ForgeActionModel but forge is useless for the moment
            int forgeD = rp.getDurationEvaluators().evaluate(ForgeVM.class, vm);
            s.post(s.geq(this.dSlice.getStart(), ChocoUtils.mult(s, method, forgeD)));

            s.post(new BooleanChanneling(method, duration, reInstantiateDuration));
        }
        state = s.makeConstantIntVar(1);
    }

    private void prepareRelocationMethod() throws SolverException {
        Model mo = rp.getSourceModel();
        Boolean cloneable = mo.getAttributes().getBoolean(vm, "clone");
        DurationEvaluators dev = rp.getDurationEvaluators();
        CPSolver s = rp.getSolver();
        int migrateDuration = dev.evaluate(MigrateVM.class, vm);
        if (Boolean.TRUE.equals(cloneable) && mo.getAttributes().isSet(vm, "template")) {
            method = rp.getSolver().createBooleanVar(rp.makeVarLabel("relocation_method(", vm, ")"));
            int bootDuration = dev.evaluate(BootVM.class, vm);
            int shutdownDuration = dev.evaluate(ShutdownVM.class, vm);
            reInstantiateDuration = bootDuration + shutdownDuration;
            duration = s.createEnumIntVar(rp.makeVarLabel("relocatable(", vm, ").duration"),
                    new int[]{0, Math.min(migrateDuration, reInstantiateDuration),
                            Math.max(migrateDuration, reInstantiateDuration)});
        } else {
            method = rp.getSolver().createIntegerConstant(rp.makeVarLabel("relocation_method(", vm, ")"), 0);
            duration = s.createEnumIntVar(rp.makeVarLabel("relocatable(", vm, ").duration"), new int[]{0, migrateDuration});
        }
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        DurationEvaluators dev = rp.getDurationEvaluators();
        if (cSlice.getHoster().getVal() != dSlice.getHoster().getVal()) {
            Action a;
            UUID dst = rp.getNode(dSlice.getHoster().getVal());
            if (method.isInstantiatedTo(0)) {
                int st = getStart().getVal();
                int ed = getEnd().getVal();
                a = new MigrateVM(vm, src, dst, st, ed);
                plan.add(a);
            } else {
                try {
                    UUID newVM = rp.cloneVM(vm);
                    if (newVM == null) {
                        rp.getLogger().error("Unable to get a new UUID to plan the re-instantiate of VM {}", vm);
                        return false;
                    }
                    ForgeVM fvm = new ForgeVM(newVM, dSlice.getStart().getVal() - dev.evaluate(ForgeVM.class, vm), dSlice.getStart().getVal());
                    //forge the new VM from a template
                    plan.add(fvm);
                    //Boot the new VM
                    int endForging = fvm.getEnd();
                    BootVM boot = new BootVM(newVM, dst, endForging, endForging + dev.evaluate(BootVM.class, newVM));
                    boot.addEvent(Action.Hook.pre, new SubstitutedVMEvent(vm, newVM));
                    return plan.add(boot) && plan.add(new ShutdownVM(vm, src, boot.getEnd(), cSlice.getEnd().getVal()));
                } catch (SolverException ex) {
                    rp.getLogger().error(ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public UUID getVM() {
        return vm;
    }

    @Override
    public IntDomainVar getStart() {
        return dSlice.getStart();
    }

    @Override
    public IntDomainVar getEnd() {
        return cSlice.getEnd();
    }

    @Override
    public IntDomainVar getDuration() {
        return duration;
    }

    @Override
    public Slice getCSlice() {
        return cSlice;
    }

    @Override
    public Slice getDSlice() {
        return dSlice;
    }

    @Override
    public IntDomainVar getState() {
        return state;
    }

    @Override
    public void visit(ActionModelVisitor v) {
        v.visit(this);
    }

    @Override
    public IntDomainVar isStaying() {
        return stay;
    }

    /**
     * Tells if the VM can be migrated or re-instantiated.
     *
     * @return a variable instantiated to {@code 0} for a migration based relocation or {@code 1}
     *         for a re-instantiation based relocation
     */
    public IntDomainVar getRelocationMethod() {
        return method;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("relocate(method=");
        b.append(prettyMethod(method));
        b.append(" ,vm=").append(vm)
                .append(" ,from=").append(src)
                .append("(").append(rp.getNode(src)).append(")")
                .append(" ,to=").append(dSlice.getHoster().getDomain().pretty())
                .append(")");
        return b.toString();
    }

    private static String prettyMethod(IntDomainVar method) {
        if (method.isInstantiatedTo(0)) {
            return "migration";
        } else if (method.isInstantiatedTo(1)) {
            return "re-instantiation";
        }
        return "(migration || re-instantiation)";
    }
}
