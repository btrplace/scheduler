/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceBuilder;
import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.LogicalConstraintFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.Task;
import solver.variables.VariableFactory;


/**
 * Model an action that allow a running VM to be relocate elsewhere if necessary.
 * The relocation can be performed through a live-migration or a re-instantiation.
 * The re-instantiation consists in forging a new VM having the same characteristics
 * and launching it on the destination node. Once this new VM has been launched, the
 * original VM is shut down. Such a relocation method may be faster than a migration-based
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

    private final VM vm;

    private IntVar state;

    private IntVar duration;

    private BoolVar stay;

    private int reInstantiateDuration;

    private Node src;

    /**
     * The relocation method. 0 for migration, 1 for relocation.
     */
    private BoolVar method;

    /**
     * Make a new model.
     *
     * @param p the RP to use as a basis.
     * @param e the VM managed by the action
     * @throws SolverException if an error occurred
     */
    public RelocatableVMModel(ReconfigurationProblem p, VM e) throws SolverException {
        this.vm = e;
        this.rp = p;

        src = p.getSourceModel().getMapping().getVMLocation(e);


        prepareRelocationMethod();

        Solver s = p.getSolver();

        cSlice = new SliceBuilder(p, e, "relocatable(" + e + ").cSlice")
                .setHoster(p.getNode(p.getSourceModel().getMapping().getVMLocation(e)))
                .setEnd(p.makeUnboundedDuration("relocatable(" + e + ").cSlice_end"))
                .build();

        dSlice = new SliceBuilder(p, vm, "relocatable(" + vm + ").dSlice")
                .setStart(p.makeUnboundedDuration("relocatable(", vm, ").dSlice_start"))
                .build();

        BoolVar move = VariableFactory.bool(p.makeVarLabel("relocatable(", vm, ").move"), s);
        Constraint cstr = IntConstraintFactory.arithm(cSlice.getHoster(), "!=", dSlice.getHoster());
        LogicalConstraintFactory.reification(move, cstr);
        //s.post(ReifiedFactory.builder(move, s.neq(cSlice.getHoster(), dSlice.getHoster()), s));

        stay = VariableFactory.not(move);//new BoolVarNot(s, p.makeVarLabel("relocatable(", e, ").stay"), (BooleanVarImpl) move);

        s.post(IntConstraintFactory.arithm(duration, "<=", cSlice.getDuration()));
        //s.post(s.leq(duration, cSlice.getDuration()));
        //s.post(s.leq(duration, dSlice.getDuration()));
        s.post(IntConstraintFactory.arithm(duration, "<=", dSlice.getDuration()));
        Task t = VariableFactory.task(dSlice.getStart(), duration, cSlice.getEnd());
        //s.post(s.eq(cSlice.getEnd(), s.plus(dSlice.getStart(), duration)));

        //s.post(s.leq(cSlice.getDuration(), p.getEnd()));
        s.post(IntConstraintFactory.arithm(cSlice.getDuration(), "<=", p.getEnd()));
        //s.post(s.leq(dSlice.getDuration(), p.getEnd()));
        s.post(IntConstraintFactory.arithm(dSlice.getDuration(), "<=", p.getEnd()));
        //s.post(s.leq(dSlice.getEnd(), p.getEnd()));
        s.post(IntConstraintFactory.arithm(dSlice.getEnd(), "<=", p.getEnd()));

        //If we allow re-instantiate, then the dSlice duration will consume necessarily after the forgeDuration
        s.post(IntConstraintFactory.boolean_channeling(new BoolVar[]{stay}, duration, 0));
        //s.post(new BooleanChanneling(stay, duration, 0));

        if (!getRelocationMethod().instantiated()) {
            //TODO: not very compliant with the ForgeActionModel but forge is useless for the moment
            int forgeD = p.getDurationEvaluators().evaluate(p.getSourceModel(), ForgeVM.class, vm);
            IntVar time = VariableFactory.bounded(rp.makeVarLabel(method.getName(), "x", forgeD), 0, Integer.MAX_VALUE, s);
            IntConstraintFactory.times(method, VariableFactory.fixed(forgeD, s), time);
            s.post(IntConstraintFactory.arithm(this.dSlice.getStart(), ">=", time));
            //s.post(s.geq(this.dSlice.getStart(), ChocoUtils.mult(s, method, forgeD)));

            //s.post(new BooleanChanneling(method, duration, reInstantiateDuration));
            s.post(IntConstraintFactory.boolean_channeling(new BoolVar[]{method}, duration, reInstantiateDuration));
        }
        state = VariableFactory.one(rp.getSolver());
    }

    private void prepareRelocationMethod() throws SolverException {
        Model mo = rp.getSourceModel();
        Boolean cloneable = mo.getAttributes().getBoolean(vm, "clone");
        DurationEvaluators dev = rp.getDurationEvaluators();
        Solver s = rp.getSolver();
        int migrateDuration = dev.evaluate(rp.getSourceModel(), MigrateVM.class, vm);
        if (Boolean.TRUE.equals(cloneable) && mo.getAttributes().isSet(vm, "template")) {
            method = VariableFactory.bool(rp.makeVarLabel("relocation_method(", vm, ")"), s);
            int bootDuration = dev.evaluate(rp.getSourceModel(), BootVM.class, vm);
            int shutdownDuration = dev.evaluate(rp.getSourceModel(), ShutdownVM.class, vm);
            reInstantiateDuration = bootDuration + shutdownDuration;
            duration = VariableFactory.enumerated(rp.makeVarLabel("relocatable(", vm, ").duration"),
                    new int[]{0, Math.min(migrateDuration, reInstantiateDuration),
                            Math.max(migrateDuration, reInstantiateDuration)}, s);
        } else {
            method = VariableFactory.zero(rp.getSolver());//rp.getSolver().createIntegerConstant(rp.makeVarLabel("relocation_method(", vm, ")"), 0);
            duration = VariableFactory.enumerated(rp.makeVarLabel("relocatable(", vm, ").duration"), new int[]{0, migrateDuration}, s);
        }
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        DurationEvaluators dev = rp.getDurationEvaluators();
        if (cSlice.getHoster().getValue() != dSlice.getHoster().getValue()) {
            Action a;
            Node dst = rp.getNode(dSlice.getHoster().getValue());
            if (method.instantiatedTo(0)) {
                int st = getStart().getValue();
                int ed = getEnd().getValue();
                a = new MigrateVM(vm, src, dst, st, ed);
                plan.add(a);
            } else {
                try {
                    VM newVM = rp.cloneVM(vm);
                    if (newVM == null) {
                        rp.getLogger().error("Unable to get a new int to plan the re-instantiate of VM {}", vm);
                        return false;
                    }
                    ForgeVM fvm = new ForgeVM(newVM, dSlice.getStart().getValue() - dev.evaluate(rp.getSourceModel(), ForgeVM.class, vm), dSlice.getStart().getValue());
                    //forge the new VM from a template
                    plan.add(fvm);
                    //Boot the new VM
                    int endForging = fvm.getEnd();
                    BootVM boot = new BootVM(newVM, dst, endForging, endForging + dev.evaluate(rp.getSourceModel(), BootVM.class, newVM));
                    boot.addEvent(Action.Hook.pre, new SubstitutedVMEvent(vm, newVM));
                    return plan.add(boot) && plan.add(new ShutdownVM(vm, src, boot.getEnd(), cSlice.getEnd().getValue()));
                } catch (SolverException ex) {
                    rp.getLogger().error(ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public VM getVM() {
        return vm;
    }

    @Override
    public IntVar getStart() {
        return dSlice.getStart();
    }

    @Override
    public IntVar getEnd() {
        return cSlice.getEnd();
    }

    @Override
    public IntVar getDuration() {
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
    public IntVar getState() {
        return state;
    }

    @Override
    public void visit(ActionModelVisitor v) {
        v.visit(this);
    }

    @Override
    public IntVar isStaying() {
        return stay;
    }

    /**
     * Tells if the VM can be migrated or re-instantiated.
     *
     * @return a variable instantiated to {@code 0} for a migration based relocation or {@code 1}
     * for a re-instantiation based relocation
     */
    public IntVar getRelocationMethod() {
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
                .append(" ,to=").append(dSlice.getHoster().toString())
                .append(")");
        return b.toString();
    }

    private static String prettyMethod(IntVar method) {
        if (method.instantiatedTo(0)) {
            return "migration";
        } else if (method.instantiatedTo(1)) {
            return "re-instantiation";
        }
        return "(migration || re-instantiation)";
    }
}
