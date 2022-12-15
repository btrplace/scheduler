/*
 * Copyright  2022 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions;

import org.btrplace.scheduler.choco.transition.KeepRunningVM;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import java.util.BitSet;

/**
 * This constraint manages the VMs that are keep running.
 * In practice, it enforces that:
 * - KeepRunningVM.isStaying() is true iff the dslice and the cslice are colocated
 * - KeeRunningVM.getDuration() is 0 iff isStaying it true
 * - When KeepRunningVM.isStaying(), if the VM global profile is staying the same or decreasing,
 * then the cSlice duration is set to 0 (early resource de-allocation). Otherwise, the dSlice
 * duration is set to 0 (late resource allocation).
 */
public class StayingVMsScheduling extends Propagator<IntVar> {

    /**
     * Managed actions.
     */
    private final KeepRunningVM[] actions;

    /**
     * State which of the actions refer to a decreasing or a still VM profile.
     */
    private final BitSet decreasingOrStill;

    /**
     * Extract the dslices from the given actions.
     *
     * @param actions the actions.
     * @return an array of dslices.
     */
    public static IntVar[] dSlices(final KeepRunningVM[] actions) {
        final IntVar[] hosters = new IntVar[actions.length];
        for (int i = 0; i < hosters.length; i++) {
            hosters[i] = actions[i].getDSlice().getHoster();
        }
        return hosters;
    }

    /**
     * New propagator.
     *
     * @param actions           the managed actions.
     * @param decreasingOrStill true for the actions having a decreasing or still profile.
     */
    public StayingVMsScheduling(final KeepRunningVM[] actions, final BitSet decreasingOrStill) {
        // Get updates for the placement variables only. It could also be notified with staying or duration variables
        // but in practice, the solver always focuses on the placement variables in prior so no need to be awake too
        // often.
        super(dSlices(actions), PropagatorPriority.LINEAR, true);
        this.actions = actions;
        this.decreasingOrStill = decreasingOrStill;
    }

    @Override
    public void propagate(final int evtmask) throws ContradictionException {
        // Full propagation. Just check every placement variable.
        for (int i = actions.length - 1; i >= 0; i--) {
            propagate(i, 0); // Mask does not matter.
        }
    }

    @Override
    public void propagate(int idx, int mask) throws ContradictionException {
        // Fine grain propagation. Placement variable var[idx] is instantiated.
        final int curPosition = actions[idx].getCSlice().getHoster().getLB();
        filterFromHost(idx, curPosition, vars[idx], actions[idx].isStaying(), actions[idx].getDuration());
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.combine(IntEventType.REMOVE, IntEventType.INSTANTIATE);
    }

    private void filterFromHost(final int idx, final int cur, final IntVar host, final IntVar stay, final IntVar dur)
            throws ContradictionException {
        if (host.isInstantiatedTo(cur)) {
            // The VM stays on its current node.
            // 0 action duration.
            dur.instantiateTo(0, this);
            // It stays.
            stay.instantiateTo(1, this);
            if (decreasingOrStill.get(idx)) {
                // Decreasing or still profile. The cslice duration is set to 0 so the dslice will span up to the end.
                // This is an early de-allocation.
                actions[idx].getCSlice().getDuration().instantiateTo(0, this);
            } else {
                // Increasing profile. The dSlice duration is set to 0 so the cslice will span up to the end. This is
                // an allocation at last.
                actions[idx].getDSlice().getDuration().instantiateTo(0, this);
            }
        } else if (!host.contains(cur)) {
            // The VM moves away. Non 0 duration.
            dur.removeValue(0, this);
            stay.instantiateTo(0, this);
        }
    }

    @Override
    public ESat isEntailed() {
        // Check every task, undefined or false status being dominant.
        for (int i = actions.length - 1; i >= 0; i--) {
            final KeepRunningVM trans = actions[i];
            final int cur = trans.getCSlice().getHoster().getLB();
            if (vars[i].isInstantiatedTo(cur)) {
                // The VM stays on its current node.
                if (!trans.getDuration().isInstantiatedTo(0)) {
                    return ESat.FALSE;
                }
                if (!trans.isStaying().isInstantiatedTo(1)) {
                    return ESat.FALSE;
                }
                if (decreasingOrStill.get(i)) {
                    if (!trans.getCSlice().getDuration().isInstantiatedTo(0)) {
                        return ESat.FALSE;
                    }
                } else {
                    if (!trans.getDSlice().getDuration().isInstantiatedTo(0)) {
                        return ESat.FALSE;
                    }
                }
            } else if (!vars[i].contains(cur)) {
                // The VM moved away.
                if (trans.getDuration().isInstantiatedTo(0)) {
                    return ESat.FALSE;
                }
                if (!trans.isStaying().isInstantiatedTo(0)) {
                    return ESat.FALSE;
                }
            } else {
                return ESat.UNDEFINED;
            }
        }
        return ESat.TRUE;
    }

    /**
     * New constraint.
     *
     * @param actions           all the KeepRunning actions.
     * @param decreasingOrStill the bitset stating which of the actions represent a VM with a
     *                          decreasing or stalling profile.
     */
    public static Constraint newConstraint(final KeepRunningVM[] actions, final BitSet decreasingOrStill) {
        return new Constraint("StayingVMs", new StayingVMsScheduling(actions, decreasingOrStill));
    }
}
