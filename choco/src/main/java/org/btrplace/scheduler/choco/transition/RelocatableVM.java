/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.VMState;
import org.btrplace.model.view.network.Network;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.SubstitutedVMEvent;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.SliceBuilder;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.btrplace.scheduler.choco.extensions.FastIFFEq;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;


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
 * @author Vincent Kherbache
 */
public class RelocatableVM implements KeepRunningVM {

  public static final String PREFIX = "relocatable(";
  public static final String PREFIX_STAY = "stayRunningOn(";
  private final VM vm;
  private final Slice cSlice;
  private final Slice dSlice;
  private final ReconfigurationProblem rp;
  private final BoolVar state;
  private final BoolVar stay;
  private IntVar duration;
  private IntVar start;
  private IntVar end;
  private IntVar bandwidth;
  private final Node src;
  private boolean manageable = true;
  private boolean postCopy = false;
  private Task migrationTask;
  /**
   * The relocation method. 0 for migration, 1 for relocation.
   */
  private final BoolVar doReinstantiation;

    /**
     * Make a new model.
     *
     * @param p the RP to use as a basis.
     * @param e the VM managed by the action
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred
     */
    public RelocatableVM(ReconfigurationProblem p, VM e) throws SchedulerException {

        // Get vars
        vm = e;
        rp = p;
        src = rp.getSourceModel().getMapping().getVMLocation(e);
        org.chocosolver.solver.Model csp = rp.getModel();
        Model mo = rp.getSourceModel();

        // Default values
        start = rp.getStart();
        end = rp.getStart();
        duration = csp.intVar(0);
        state = csp.boolVar(true);
        
        // If not manageable, the VM stays on the current host
        if (!p.getManageableVMs().contains(e)) {
            stay = csp.boolVar(true);
            doReinstantiation = csp.boolVar(false);
            manageable = false;
            
            IntVar host;
            if (rp.labelVariables()) {
                host = rp.makeCurrentHost(vm, PREFIX_STAY, vm, ").host");
            } else {
                host = rp.makeCurrentHost(vm);
            }
            cSlice = new SliceBuilder(rp, vm, PREFIX_STAY, vm.toString(), ").cSlice")
                    .setHoster(host)
                    .setEnd(rp.makeUnboundedDuration(PREFIX_STAY, vm, ").cSlice_end"))
                    .build();
            dSlice = new SliceBuilder(rp, vm, PREFIX_STAY, vm, ").dSlice")
                    .setHoster(host)
                    .setStart(cSlice.getEnd())
                    .build();

            return;
        }

        // The VM can move (to re-instantiate or migrate) OR STAY to the same host
        cSlice = new SliceBuilder(rp, vm, PREFIX, vm, ").cSlice")
                .setHoster(rp.getNode(rp.getSourceModel().getMapping().getVMLocation(vm)))
                .setEnd(rp.makeUnboundedDuration(PREFIX, vm, ").cSlice_end"))
                .build();

        dSlice = new SliceBuilder(rp, vm, PREFIX, vm, ").dSlice")
                .setStart(rp.makeUnboundedDuration(PREFIX, vm, ").dSlice_start"))
                .build();

        // Update start and end vars of the action
        start = dSlice.getStart();
        end = cSlice.getEnd();

        csp.post(rp.getModel().arithm(end, "<=", rp.getEnd()));
        // Get some static durations from evaluators
        DurationEvaluators dev = rp.getDurationEvaluators();
        int migrateDuration = dev.evaluate(rp.getSourceModel(), MigrateVM.class, vm);
        int bootDuration = dev.evaluate(rp.getSourceModel(), org.btrplace.plan.event.BootVM.class, vm);
        int forgeD = p.getDurationEvaluators().evaluate(p.getSourceModel(), org.btrplace.plan.event.ForgeVM.class, vm);

        // Compute the re-instantiation duration
        int reInstantiateDuration = bootDuration + forgeD;
        reInstantiateDuration = forgeD; // Compliant with CMaxOnlineTest and others
        
        // Get the networking view if attached
        Network network = Network.get(mo);
        IntVar migrationDuration;
        if (network != null) {

            // Set the migration algorithm
            postCopy = mo.getAttributes().get(vm, "postCopy", false);

            // Create unbounded/large domain vars for migration duration and bandwidth
            migrationDuration = p.makeUnboundedDuration("migration(", vm, ").duration");
            bandwidth = csp.intVar(PREFIX + vm + ").bandwidth", 0, Integer.MAX_VALUE / 100, true);
        }
        // No networking view, set the duration from the evaluator
        else {
            // The duration can still be 0 => the VM STAY !
            if (rp.labelVariables()) {
                migrationDuration = csp.intVar(rp.makeVarLabel("migration(", vm, ").duration"),
                        new int[]{0, migrateDuration});
            } else {
                migrationDuration = csp.intVar(new int[]{0, migrateDuration});
            }
            bandwidth = null;
        }

        // Possibly re-instantiate (if some attributes are defined)
        if (mo.getAttributes().get(vm, "clone", false) && mo.getAttributes().isSet(vm, "template")) {
            if (rp.labelVariables()) {
                doReinstantiation = csp.boolVar(rp.makeVarLabel("relocation_method(", vm, ")"));
            } else {
                doReinstantiation = csp.boolVar("");
            }

            if (rp.labelVariables()) {
                duration = csp.intVar(rp.makeVarLabel(PREFIX, vm, ").duration"),
                        Math.min(migrationDuration.getLB(), reInstantiateDuration),
                        Math.max(migrationDuration.getUB(), reInstantiateDuration), true);
            } else {
                duration = csp.intVar("", Math.min(migrationDuration.getLB(), reInstantiateDuration),
                        Math.max(migrationDuration.getUB(), reInstantiateDuration), true);
            }

            // Re-instantiate or migrate
            // (Prefer the re-instantiation if the duration are the same, otherwise choose the min)
            rp.getModel().ifThenElse(rp.getModel().or(rp.getModel().arithm(doReinstantiation, "=", 0), // can be instantiated externally !
                    rp.getModel().arithm(migrationDuration, "<", reInstantiateDuration)),
                    rp.getModel().arithm(duration, "=", migrationDuration),
                    rp.getModel().arithm(duration, "=", reInstantiateDuration)
            );

            // If it is a re-instantiation then specify that the dSlice must start AFTER the Forge delay
            IntVar time;
            if (rp.labelVariables()) {
                time = csp.intVar(rp.makeVarLabel(doReinstantiation.getName(), " * ", forgeD),
                        0, forgeD, false);
            } else {
                time = csp.intVar("", 0, forgeD, false);
            }
            csp.post(csp.times(doReinstantiation, forgeD, time));
            csp.post(rp.getModel().arithm(start, ">=", time));
            // Be sure that doReinstantiation will be instantiated
            csp.post(new FastIFFEq(doReinstantiation, duration, reInstantiateDuration));
        }
        // The VM either migrate or stay but won't be re-instantiated for sure
        else {
            doReinstantiation = csp.boolVar(false);
            duration = migrationDuration;
        }

        // If the VM stay (src host == dst host), then duration = 0
        stay = rp.labelVariables() ? csp.boolVar(rp.makeVarLabel(vm, "stay")) : csp.boolVar("");
        csp.post(new FastIFFEq(stay, dSlice.getHoster(), cSlice.getHoster().getValue()));
        csp.post(new FastIFFEq(stay, duration, 0));
        //We have to force the migration duration equals to 0 if it stays
        //otherwise, the variable will be free
        csp.post(new FastIFFEq(stay, migrationDuration, 0));

        // Create the task ('default' cumulative constraint with a height of 1)
        migrationTask = new Task(start, duration, end);
    }

    /**
     * Get the task associated to the migration
     *
     * @return a task
     */
    public Task getMigrationTask() {
        return migrationTask;
    }

    private static String prettyMethod(IntVar method) {
        if (method.isInstantiatedTo(0)) {
            return "migration";
        } else if (method.isInstantiatedTo(1)) {
            return "re-instantiation";
        }
        return "(migration || re-instantiation)";
    }

    @Override
    public boolean isManaged() {
        return manageable;
    }

    @Override
    public boolean insertActions(Solution s, ReconfigurationPlan plan) {
        DurationEvaluators dev = rp.getDurationEvaluators();
        // Only if the VM doesn't stay
        if (s.getIntVal(cSlice.getHoster()) != (s.getIntVal(dSlice.getHoster()))) {
            Action a;
            Node dst = rp.getNode(s.getIntVal(dSlice.getHoster()));
            // Migration
            if (s.getIntVal(doReinstantiation) == 0) {
                int st = s.getIntVal(getStart());
                int ed = s.getIntVal(getEnd());

                if (getBandwidth() != null) {
                    a = new MigrateVM(vm, src, dst, st, ed, s.getIntVal(getBandwidth()));
                }
                else {
                    a = new MigrateVM(vm, src, dst, st, ed);
                }
                plan.add(a);
            // Re-instantiation
            } else {
                    VM newVM = rp.cloneVM(vm);
                    if (newVM == null) {
                        rp.getLogger().debug("Unable to get a new int to plan the re-instantiate of VM {}", vm);
                        return false;
                    }
                    org.btrplace.plan.event.ForgeVM fvm = new org.btrplace.plan.event.ForgeVM(
                            newVM,
                            s.getIntVal(dSlice.getStart()) - 
                                    dev.evaluate(rp.getSourceModel(), org.btrplace.plan.event.ForgeVM.class, vm),
                            s.getIntVal(dSlice.getStart())
                    );
                    //forge the new VM from a template
                plan.add(fvm);
                    //Boot the new VM
                    int endForging = fvm.getEnd();
                    org.btrplace.plan.event.BootVM boot = new org.btrplace.plan.event.BootVM(
                            newVM,
                            dst,
                            endForging,
                            endForging + dev.evaluate(rp.getSourceModel(), org.btrplace.plan.event.BootVM.class, newVM)
                    );
                    boot.addEvent(Action.Hook.PRE, new SubstitutedVMEvent(vm, newVM));
                    return plan.add(boot) && plan.add(new org.btrplace.plan.event.ShutdownVM(
                                                        vm, src, boot.getEnd(), s.getIntVal(cSlice.getEnd())));
            }
        }
        return true;
    }

    public IntVar getBandwidth() {
        return bandwidth;
    }

    public boolean usesPostCopy() { return postCopy; }

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
                " ,to=" + (dSlice.getHoster().isInstantiated() ? rp.getNode(dSlice.getHoster().getValue()) : dSlice.getHoster().toString()) + ")";
    }

    @Override
    public VMState getSourceState() {
        return VMState.RUNNING;
    }

    @Override
    public VMState getFutureState() {
        return VMState.RUNNING;
    }

    /**
     * The builder devoted to a running &gt; running transition.
     */
    public static class Builder extends VMTransitionBuilder {

        /**
         * New builder
         */
        public Builder() {
            super("relocatable", VMState.RUNNING, VMState.RUNNING);
        }

        @Override
        public VMTransition build(ReconfigurationProblem r, VM v) throws SchedulerException {
            return new RelocatableVM(r, v);
        }
    }
}
