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

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.Gather;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.*;

/**
 * Choco implementation of {@link org.btrplace.model.constraint.Gather}.
 *
 * @author Fabien Hermenier
 */
public class CGather implements ChocoConstraint {

    private Gather cstr;

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
    public boolean inject(ReconfigurationProblem rp) {
        List<Slice> dSlices = getDSlices(rp);
        if (cstr.isContinuous()) {
            //Check for the already running VMs
            Mapping map = rp.getSourceModel().getMapping();
            Node loc = null;
            for (VM vm : cstr.getInvolvedVMs()) {
                if (map.isRunning(vm)) {
                    Node node = map.getVMLocation(vm);
                    if (loc == null) {
                        loc = node;
                    } else if (!loc.equals(node)) {
                        rp.getLogger().error("Some VMs in '{}' are already running but not co-located", cstr.getInvolvedVMs());
                        return false;
                    }
                }
            }
            if (loc != null) {
                return placeDSlices(rp, dSlices, rp.getNode(loc));
            } else {
                return forceDiscreteCollocation(rp, dSlices);
            }
        }
        return forceDiscreteCollocation(rp, dSlices);
    }

    private boolean placeDSlices(ReconfigurationProblem rp, List<Slice> dSlices, int nIdx) {
        for (Slice s : dSlices) {
            try {
                s.getHoster().instantiateTo(nIdx, Cause.Null);
            } catch (ContradictionException ex) {
                rp.getLogger().error("Unable to maintain the co-location of all the future-running VMs in '{}': ", cstr.getInvolvedVMs());
                return false;
            }
        }
        return true;
    }

    private boolean forceDiscreteCollocation(ReconfigurationProblem rp, List<Slice> dSlices) {
        Solver s = rp.getSolver();
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
                s.post(IntConstraintFactory.arithm(i1, "=", i2));
            }
        }
        return true;
    }

    private static boolean instantiateTo(ReconfigurationProblem rp, IntVar i, int v, Slice s1, Slice s2) {
        try {
            i.instantiateTo(v, Cause.Null);
        } catch (ContradictionException ex) {
            rp.getLogger().error("Unable to force VM '" + s1.getSubject() + "' to be co-located with VM '" + s2.getSubject() + "'");
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return cstr.toString();
    }


    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        if (!cstr.isSatisfied(m)) {
            return new HashSet<>(cstr.getInvolvedVMs());
        }
        return Collections.emptySet();
    }

    /**
     * The builder associated to that constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return Gather.class;
        }

        @Override
        public CGather build(Constraint c) {
            return new CGather((Gather) c);
        }
    }
}
