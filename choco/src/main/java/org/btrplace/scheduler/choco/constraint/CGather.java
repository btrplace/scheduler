/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Gather;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.*;

/**
 * Choco implementation of {@link org.btrplace.model.constraint.Gather}.
 *
 * @author Fabien Hermenier
 */
public class CGather implements ChocoConstraint {

  private final Gather cstr;

    /**
     * Make a new constraint.
     *
     * @param g the constraint to rely on
     */
    public CGather(Gather g) {
        cstr = g;
    }

    private List<Slice> getDSlices(ReconfigurationProblem rp) {
        List<Slice> dSlices = new ArrayList<>();
        for (VM vm : cstr.getInvolvedVMs()) {
            VMTransition a = rp.getVMAction(vm);
            Slice dSlice = a.getDSlice();
            if (dSlice != null) {
                dSlices.add(dSlice);
            }
        }
        return dSlices;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) {
        List<Slice> dSlices = getDSlices(rp);
        if (cstr.isContinuous() && !continuousColocation(rp, dSlices)) {
                return false;
        }
        return forceDiscreteCollocation(rp, dSlices);
    }

    /*
    * Check for the already running VMs and force co-location if any.
     */
    private boolean continuousColocation(ReconfigurationProblem rp, List<Slice> dSlices) {

        Mapping map = rp.getSourceModel().getMapping();
        Node loc = null;
        for (VM vm : cstr.getInvolvedVMs()) {
            if (map.isRunning(vm)) {
                Node node = map.getVMLocation(vm);
                if (loc == null) {
                    loc = node;
                } else if (!loc.equals(node)) {
                    rp.getLogger().debug("Some VMs in '{}' are already running but not co-located", cstr.getInvolvedVMs());
                    return false;
                }
            }
        }
        if (loc != null) {
          if (cstr.getInvolvedVMs().size() == 1 && dSlices.size() == 1) {
            // The VM may migrate, we don't care as it is alone.
            return true;
          }
            return placeDSlices(rp, dSlices, rp.getNode(loc));
        }
        return true;
    }

    private boolean placeDSlices(ReconfigurationProblem rp, List<Slice> dSlices, int nIdx) {
        for (Slice s : dSlices) {
            try {
                s.getHoster().instantiateTo(nIdx, Cause.Null);
            } catch (ContradictionException ex) {
                rp.getLogger().debug("Unable to maintain the co-location of all the future-running VMs in " + cstr.getInvolvedVMs(), ex);
                return false;
            }
        }
        return true;
    }

    private static boolean forceDiscreteCollocation(ReconfigurationProblem rp, List<Slice> dSlices) {
        for (int i = 0; i < dSlices.size(); i++) {
            for (int j = 0; j < i; j++) {
                Slice s1 = dSlices.get(i);
                Slice s2 = dSlices.get(j);
                IntVar i1 = s1.getHoster();
                IntVar i2 = s2.getHoster();
                if (i1.isInstantiated() && !instantiateTo(rp, i2, i1.getLB(), s1, s2)) {
                    return false;
                } else if (i2.isInstantiated() && !instantiateTo(rp, i1, i2.getLB(), s1, s2)) {
                    return false;
                }
                rp.getModel().post(rp.getModel().arithm(i1, "=", i2));
            }
        }
        return true;
    }

    private static boolean instantiateTo(ReconfigurationProblem rp, IntVar i, int v, Slice s1, Slice s2) {
        try {
            i.instantiateTo(v, Cause.Null);
        } catch (ContradictionException ex) {
            rp.getLogger().debug("Unable to force VM '" + s1.getSubject() + "' to be co-located with VM '" + s2.getSubject() + "'", ex);
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return cstr.toString();
    }


    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        if (!cstr.isSatisfied(i.getModel())) {
            return new HashSet<>(cstr.getInvolvedVMs());
        }
        return Collections.emptySet();
    }
}
