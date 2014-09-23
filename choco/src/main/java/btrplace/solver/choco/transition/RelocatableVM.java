/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.transition;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.VMState;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.SubstitutedVMEvent;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceBuilder;
import btrplace.solver.choco.duration.DurationEvaluators;
import btrplace.solver.choco.extensions.FastIFFEq;
import solver.Solver;
import solver.constraints.Arithmetic;
import solver.constraints.IntConstraintFactory;
import solver.constraints.Operator;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VF;
import solver.variables.VariableFactory;


/**
 * Model an action that allow a running VM to be relocate elsewhere if necessary.
 * The relocation can be performed through a live-migration or a re-instantiation.
 * The re-instantiation consists in forging a new VM having the same characteristics
 * and launching it on the destination node. Once this new VM has been launched, the
 * original VM is shut down. Such a relocation n may be faster than a migration-based
 * one while being less aggressive for the network. However, the VM must be able to
 * be cloned from a template.
 * <p>
 * If the relocation is performed with a live-migration, a {@link MigrateVM} action
 * will be generated. If the relocation is performed through a re-instantiation, a {@link ForgeVM},
 * a {@link BootVM}, and a {@link ShutdownVM} actions are generated.
 * <p>
 * To relocate the VM using a re-instantiation, the VM must first have an attribute {@code clone}
 * set to {@code true}. The re-instantiation duration is then estimated. If it is shorter than
 * the migration duration, then re-instantiation will be preferred.
 * <p>
 *
 * @author Fabien Hermenier
 */
public class RelocatableVM implements KeepRunningVM {

    public static final String PREFIX = "relocatable(";
    public static final String PREFIX_STAY = "stayRunningOn(";
    private final VM vm;
    private Slice cSlice, dSlice;
    private ReconfigurationProblem rp;
    private BoolVar state;
    private IntVar duration, start, end;
    private BoolVar stay;
    private int reInstantiateDuration;
    private Node src;
    private boolean manageable = true;
    /**
     * The relocation method. 0 for migration, 1 for relocation.
     */
    private BoolVar doReinstantiation;

    /**
     * Make a new model.
     *
     * @param p the RP to use as a basis.
     * @param e the VM managed by the action
     * @throws SolverException if an error occurred
     */
    public RelocatableVM(ReconfigurationProblem p, VM e) throws SolverException {

        this.vm = e;
        this.rp = p;

        //Default values, i.e. when there is a staying running VM
        start = rp.getStart();
        end = rp.getStart();
        duration = rp.getStart();
        state = VariableFactory.one(rp.getSolver());
        if (!p.getManageableVMs().contains(e)) {
            stayRunning();
            return;
        }

        src = p.getSourceModel().getMapping().getVMLocation(e);


        prepareRelocationMethod();

        Solver s = p.getSolver();

        cSlice = new SliceBuilder(p, e, PREFIX, e, ").cSlice")
                .setHoster(p.getNode(p.getSourceModel().getMapping().getVMLocation(e)))
                .setEnd(p.makeUnboundedDuration(PREFIX, e, ").cSlice_end"))
                .build();

        dSlice = new SliceBuilder(p, vm, PREFIX, vm, ").dSlice")
                .setStart(p.makeUnboundedDuration(PREFIX, vm, ").dSlice_start"))
                .build();

        start = dSlice.getStart();
        end = cSlice.getEnd();
        stay = VF.bool(vm + "stay", s);
        s.post(new FastIFFEq(stay, dSlice.getHoster(), cSlice.getHoster().getValue()));

        s.post(new Arithmetic(duration, Operator.LE, cSlice.getDuration()));
        s.post(new Arithmetic(duration, Operator.LE, dSlice.getDuration()));
        VariableFactory.task(dSlice.getStart(), duration, cSlice.getEnd());

        s.post(new Arithmetic(dSlice.getEnd(), Operator.LE, p.getEnd()));
        s.post(new Arithmetic(cSlice.getEnd(), Operator.LE, p.getEnd()));

        //If we allow re-instantiate, then the dSlice duration will consume necessarily after the forgeDuration
        s.post(new FastIFFEq(stay, duration, 0));

        if (!getRelocationMethod().isInstantiated()) {
            //TODO: not very compliant with the Forge transition but forge is useless for the moment
            int forgeD = p.getDurationEvaluators().evaluate(p.getSourceModel(), btrplace.plan.event.ForgeVM.class, vm);
            IntVar time = VariableFactory.bounded(rp.makeVarLabel(doReinstantiation.getName(), " * ", forgeD), 0, Integer.MAX_VALUE / 100, s);
            s.post(IntConstraintFactory.times(doReinstantiation, VariableFactory.fixed(forgeD, s), time));
            s.post(new Arithmetic(this.dSlice.getStart(), Operator.GE, time));

            s.post(new FastIFFEq(doReinstantiation, duration, reInstantiateDuration));
        }
    }

    private static String prettyMethod(IntVar method) {
        if (method.isInstantiatedTo(0)) {
            return "migration";
        } else if (method.isInstantiatedTo(1)) {
            return "re-instantiation";
        }
        return "(migration || re-instantiation)";
    }

    private void stayRunning() throws SolverException {
        IntVar host = rp.makeCurrentHost(vm, PREFIX_STAY, vm, ").host");
        cSlice = new SliceBuilder(rp, vm, PREFIX_STAY, vm.toString(), ").cSlice")
                .setHoster(host)
                .setEnd(rp.makeUnboundedDuration(PREFIX_STAY, vm, ").cSlice_end"))
                .build();
        dSlice = new SliceBuilder(rp, vm, PREFIX_STAY, vm, ").dSlice")
                .setHoster(host)
                .setStart(cSlice.getEnd())
                .build();
        stay = VariableFactory.one(rp.getSolver());
        manageable = false;
    }

    private void prepareRelocationMethod() throws SolverException {
        Model mo = rp.getSourceModel();
        Boolean cloneable = mo.getAttributes().getBoolean(vm, "clone");
        DurationEvaluators dev = rp.getDurationEvaluators();
        Solver s = rp.getSolver();
        int migrateDuration = dev.evaluate(rp.getSourceModel(), MigrateVM.class, vm);
        if (Boolean.TRUE.equals(cloneable) && mo.getAttributes().isSet(vm, "template")) {
            doReinstantiation = VariableFactory.bool(rp.makeVarLabel("relocation_method(", vm, ")"), s);
            int bootDuration = dev.evaluate(rp.getSourceModel(), btrplace.plan.event.BootVM.class, vm);
            int shutdownDuration = dev.evaluate(rp.getSourceModel(), btrplace.plan.event.ShutdownVM.class, vm);
            reInstantiateDuration = bootDuration + shutdownDuration;
            duration = VariableFactory.enumerated(rp.makeVarLabel(PREFIX, vm, ").duration"),
                    new int[]{0, Math.min(migrateDuration, reInstantiateDuration),
                            Math.max(migrateDuration, reInstantiateDuration)}, s
            );
        } else {
            doReinstantiation = VariableFactory.zero(rp.getSolver());
            duration = VariableFactory.enumerated(rp.makeVarLabel(PREFIX, vm, ").duration"), new int[]{0, migrateDuration}, s);
        }
    }

    @Override
    public boolean isManaged() {
        return manageable;
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        DurationEvaluators dev = rp.getDurationEvaluators();
        if (cSlice.getHoster().getValue() != dSlice.getHoster().getValue()) {
            assert stay.getValue() == 0;
            Action a;
            Node dst = rp.getNode(dSlice.getHoster().getValue());
            if (doReinstantiation.isInstantiatedTo(0)) {
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
                    btrplace.plan.event.ForgeVM fvm = new btrplace.plan.event.ForgeVM(newVM, dSlice.getStart().getValue() - dev.evaluate(rp.getSourceModel(), btrplace.plan.event.ForgeVM.class, vm), dSlice.getStart().getValue());
                    //forge the new VM from a template
                    plan.add(fvm);
                    //Boot the new VM
                    int endForging = fvm.getEnd();
                    btrplace.plan.event.BootVM boot = new btrplace.plan.event.BootVM(newVM, dst, endForging, endForging + dev.evaluate(rp.getSourceModel(), btrplace.plan.event.BootVM.class, newVM));
                    boot.addEvent(Action.Hook.PRE, new SubstitutedVMEvent(vm, newVM));
                    return plan.add(boot) && plan.add(new btrplace.plan.event.ShutdownVM(vm, src, boot.getEnd(), cSlice.getEnd().getValue()));
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
        return start;
    }

    @Override
    public IntVar getEnd() {
        return end;
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
    public BoolVar getState() {
        return state;
    }

    @Override
    public BoolVar isStaying() {
        return stay;
    }

    /**
     * Tells if the VM can be migrated or re-instantiated.
     *
     * @return a variable instantiated to {@code 0} for a migration based relocation or {@code 1}
     * for a re-instantiation based relocation
     */
    public IntVar getRelocationMethod() {
        return doReinstantiation;
    }

    @Override
    public String toString() {
        return "relocate(doReinstantiation=" + prettyMethod(doReinstantiation) +
                " ,vm=" + vm +
                " ,from=" + src + "(" + rp.getNode(src) + ")" +
                " ,to=" + dSlice.getHoster().toString() + ")";
    }

    /**
     * The builder devoted to a running->running transition.
     */
    public static class Builder extends VMTransitionBuilder {

        /**
         * New builder
         */
        public Builder() {
            super("relocatable", VMState.RUNNING, VMState.RUNNING);
        }

        @Override
        public VMTransition build(ReconfigurationProblem r, VM v) throws SolverException {
            return new RelocatableVM(r, v);
        }
    }
}
