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

package btrplace.solver.choco.constraint;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.Constraint;
import btrplace.model.constraint.Spread;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.actionModel.VMActionModel;
import btrplace.solver.choco.extensions.ChocoUtils;
import solver.Solver;
import solver.constraints.Arithmetic;
import solver.constraints.IntConstraintFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;

import java.util.*;

/**
 * Continuous implementation of {@link Spread}.
 *
 * @author Fabien Hermenier
 */
public class CSpread implements ChocoConstraint {

    private Spread cstr;

    /**
     * Make a new constraint.
     *
     * @param s the constraint to rely one
     */
    public CSpread(Spread s) {
        cstr = s;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) {

        List<IntVar> onlyRunnings = new ArrayList<>();
        for (VM vmId : cstr.getInvolvedVMs()) {
            if (rp.getFutureRunningVMs().contains(vmId)) {
                VMActionModel a = rp.getVMAction(vmId);
                Slice d = a.getDSlice();
                if (d != null) {
                    onlyRunnings.add(d.getHoster());
                }
            }
        }
        Solver s = rp.getSolver();
        if (!onlyRunnings.isEmpty()) {
            //The lazy spread implementation for the placement
            s.post(IntConstraintFactory.alldifferent(onlyRunnings.toArray(new IntVar[onlyRunnings.size()]), "BC"));

            if (cstr.isContinuous()) {
                VM[] vms = new VM[onlyRunnings.size()];
                int x = 0;
                for (VM vm : cstr.getInvolvedVMs()) {
                    if (rp.getFutureRunningVMs().contains(vm)) {
                        vms[x++] = vm;
                    }
                }
                for (int i = 0; i < vms.length; i++) {
                    VM vm = vms[i];
                    VMActionModel aI = rp.getVMAction(vm);
                    for (int j = 0; j < i; j++) {
                        VM vmJ = vms[j];
                        VMActionModel aJ = rp.getVMAction(vmJ);
                        Slice dI = aI.getDSlice();
                        Slice cJ = aJ.getCSlice();

                        Slice dJ = aJ.getDSlice();
                        Slice cI = aI.getCSlice();

                        //If both are currently hosted on the same node, no need to worry about non-overlapping
                        //between the c and the d-slices as it may create a non-solution
                        boolean currentlyGathered = cI != null && cJ != null && cJ.getHoster().instantiatedTo(cI.getHoster().getValue());

                        if (!currentlyGathered && dI != null && cJ != null) {
                            //No need to place the constraints if the slices do not have a chance to overlap
                            if (!(cJ.getHoster().instantiated() && !dI.getHoster().contains(cJ.getHoster().getValue()))
                                    && !(dI.getHoster().instantiated() && !cJ.getHoster().contains(dI.getHoster().getValue()))
                                    ) {
                                Arithmetic eqCstr = IntConstraintFactory.arithm(dI.getHoster(), "=", cJ.getHoster());
                                BoolVar eq = eqCstr.reif();
                                Arithmetic leqCstr = IntConstraintFactory.arithm(cJ.getEnd(), "<=", dI.getStart());
                                ChocoUtils.postImplies(s, eq, leqCstr);
                            }
                        }

                        //The inverse relation

                        if (!currentlyGathered && dJ != null && cI != null) {
                            //No need to place the constraints if the slices do not have a chance to overlap
                            if (!(cI.getHoster().instantiated() && !dJ.getHoster().contains(cI.getHoster().getValue()))
                                    && !(dJ.getHoster().instantiated() && !cI.getHoster().contains(dJ.getHoster().getValue()))
                                    ) {
                                Arithmetic eqCstr = IntConstraintFactory.arithm(dJ.getHoster(), "=", cI.getHoster());
                                BoolVar eq = eqCstr.reif();
                                Arithmetic leqCstr = IntConstraintFactory.arithm(cI.getEnd(), "<=", dJ.getStart());
                                ChocoUtils.postImplies(s, eq, leqCstr);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        Map<Node, Set<VM>> spots = new HashMap<>();
        Set<VM> bad = new HashSet<>();
        Mapping map = m.getMapping();
        for (VM vm : cstr.getInvolvedVMs()) {
            Node h = map.getVMLocation(vm);
            if (map.isRunning(vm)) {
                if (!spots.containsKey(h)) {
                    spots.put(h, new HashSet<VM>());
                }
                spots.get(h).add(vm);
            }

        }
        for (Map.Entry<Node, Set<VM>> e : spots.entrySet()) {
            if (e.getValue().size() > 1) {
                bad.addAll(e.getValue());
            }
        }
        return bad;
    }

    @Override
    public String toString() {
        return cstr.toString();
    }

    /**
     * The builder associated to the constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return Spread.class;
        }

        @Override
        public CSpread build(Constraint c) {
            return new CSpread((Spread) c);
        }
    }
}
