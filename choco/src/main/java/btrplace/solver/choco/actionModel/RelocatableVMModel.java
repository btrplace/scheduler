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

import btrplace.model.Attributes;
import btrplace.model.Model;
import btrplace.plan.Action;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.ForgeVM;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ShutdownVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.*;
import btrplace.solver.choco.chocoUtil.FastIFFEq;
import choco.cp.solver.CPSolver;
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

    private UUIDPool uuidPool;

    private boolean doReinstantiate = false;

    private Slice cSlice, dSlice;

    private ReconfigurationProblem rp;

    private final UUID vm;

    private UUID newVM;

    private IntDomainVar state;

    private IntDomainVar duration;

    private IntDomainVar stay;

    //Related to re-instantiate
    private ForgeVMModel forgeModel;

    private int newVMBootDuration;

    private UUID src;

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
        uuidPool = rp.getUUIDPool();

        src = rp.getSourceModel().getMapping().getVMLocation(e);

        int d = checkForReinstantiation();

        CPSolver s = rp.getSolver();

        duration = s.createEnumIntVar(rp.makeVarLabel("relocatable(" + e + ").duration"), new int[]{0, d});
        cSlice = new SliceBuilder(rp, e, "relocatable(" + e + ").cSlice")
                .setHoster(rp.getNode(rp.getSourceModel().getMapping().getVMLocation(e)))
                .setEnd(rp.makeDuration("relocatable(" + e + ").cSlice_end"))
                .build();

        dSlice = new SliceBuilder(rp, vm, "relocatable(" + vm + ").dSlice")
                .setStart(rp.makeDuration("relocatable(" + vm + ").dSlice_start"))
                .build();
        IntDomainVar move = s.createBooleanVar(rp.makeVarLabel("relocatable(" + (isReinstantiated() ? newVM : e) + ").move"));
        s.post(ReifiedFactory.builder(move, s.neq(cSlice.getHoster(), dSlice.getHoster()), s));

        stay = new BoolVarNot(s, rp.makeVarLabel("relocatable(" + e + ").stay"), (BooleanVarImpl) move);

        s.post(new FastIFFEq(stay, duration, 0));

        s.post(s.leq(duration, cSlice.getDuration()));
        s.post(s.leq(duration, dSlice.getDuration()));
        s.post(s.eq(cSlice.getEnd(), s.plus(dSlice.getStart(), duration)));

        s.post(s.leq(cSlice.getDuration(), rp.getEnd()));
        s.post(s.leq(dSlice.getDuration(), rp.getEnd()));

        if (doReinstantiate) {
            int forgeD = rp.getDurationEvaluators().evaluate(ForgeVM.class, vm);
            IntDomainVar forgeCost = s.createEnumIntVar(rp.makeVarLabel("forge(" + newVM + ")"), new int[]{0, forgeD});

            s.post(new FastIFFEq(stay, forgeCost, 0));
            s.post(s.geq(this.dSlice.getStart(), forgeCost));
        }
        state = s.makeConstantIntVar(1);
    }

    /**
     * Check if the VM will be re-instantiated or migrated.
     * The VM is re-instantiated iff its {@code clone} attribute
     * is set and if the estimated duration of the re-instantiation
     * is <= the estimated duration of the migration.
     *
     * @return the estimated duration of the action
     * @throws SolverException
     */
    private int checkForReinstantiation() throws SolverException {
        DurationEvaluators dev = rp.getDurationEvaluators();
        int migD = dev.evaluate(MigrateVM.class, vm);
        Model mo = rp.getSourceModel();
        Boolean cloneable = mo.getAttributes().getBoolean(vm, "clone");
        if (Boolean.TRUE.equals(cloneable)) {
            newVMBootDuration = dev.evaluate(BootVM.class, vm);
            int oldVMShutdownDuration = dev.evaluate(ShutdownVM.class, vm);
            int reInstantD = dev.evaluate(ForgeVM.class, vm)
                    + newVMBootDuration
                    + oldVMShutdownDuration;
            if (reInstantD <= migD) {
                doReinstantiate = true;
                newVM = uuidPool.request();
                //Copy all the attributes of vm to newVM
                Attributes attrs = mo.getAttributes();
                for (String k : attrs.getKeys(vm)) {
                    attrs.castAndPut(newVM, k, attrs.get(vm, k).toString());
                }
                if (newVM == null) {
                    throw new SolverException(mo, "Unable to get a new UUID to allow the re-instantiation of '" + vm + "'");
                }
                forgeModel = new ForgeVMModel(rp, newVM);
                return newVMBootDuration + oldVMShutdownDuration;
            }
        }
        return migD;
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        if (cSlice.getHoster().getVal() != dSlice.getHoster().getVal()) {
            Action a;
            UUID dst = rp.getNode(dSlice.getHoster().getVal());
            if (!isReinstantiated()) {
                int st = getStart().getVal();
                int ed = getEnd().getVal();
                a = new MigrateVM(vm, src, dst, st, ed);
                plan.add(a);
                rp.insertNotifyAllocations(a, vm, Action.Hook.post);
            } else {
                //forge the new VM from a template
                if (!forgeModel.insertActions(plan)) {
                    return false;
                }
                //Boot the new VM
                int endForging = forgeModel.getEnd().getVal();
                BootVM boot = new BootVM(forgeModel.getVM(), dst, endForging, endForging + newVMBootDuration);
                //This notification is about the old VM. This is needed to satisfy potential constraints looking
                //at the old VM UUID
                rp.insertNotifyAllocations(boot, vm, Action.Hook.pre);
                //We replicate the Event on the new VM
                rp.insertNotifyAllocations(boot, newVM, Action.Hook.pre);
                return plan.add(boot) && plan.add(new ShutdownVM(vm, src, boot.getEnd(), cSlice.getEnd().getVal()));
            }
        } else {
            int st = dSlice.getStart().getVal();
            rp.insertAllocateAction(plan, vm, src, st, st);
            if (doReinstantiate) {
                uuidPool.release(newVM);
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
     * Tells if the VM will be relocated (if needed)
     * using the re-instantiation method.
     *
     * @return {@code true} if the re-instantiation method is preferred over the migration method
     */
    public boolean isReinstantiated() {
        return doReinstantiate;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("relocate(method=");
        b.append(isReinstantiated() ? "re-instantiate" : "migrate");
        b.append(" ,vm=").append(vm)
                .append(" ,from=").append(src)
                .append("(").append(rp.getNode(src)).append(")")
                .append(" ,to=").append(dSlice.getHoster().getDomain().pretty())
                .append(")");
        return b.toString();
    }
}
