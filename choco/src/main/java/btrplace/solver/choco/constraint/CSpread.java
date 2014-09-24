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

package btrplace.solver.choco.constraint;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.Constraint;
import btrplace.model.constraint.Spread;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.extensions.ChocoUtils;
import btrplace.solver.choco.transition.VMTransition;
import solver.Solver;
import solver.constraints.Arithmetic;
import solver.constraints.IntConstraintFactory;
import solver.constraints.Operator;
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

        List<IntVar> running = placementVariables(rp);

        Solver s = rp.getSolver();
        if (running.isEmpty()) {
            return true;
        }

        //The lazy spread implementation for the placement
        s.post(IntConstraintFactory.alldifferent(running.toArray(new IntVar[running.size()]), "BC"));

        if (cstr.isContinuous()) {
            VM[] vms = new VM[running.size()];
            int x = 0;
            for (VM vm : cstr.getInvolvedVMs()) {
                vms[x++] = vm;
            }
            for (int i = 0; i < vms.length; i++) {
                VM vm = vms[i];
                VMTransition aI = rp.getVMAction(vm);
                for (int j = 0; j < i; j++) {
                    VM vmJ = vms[j];
                    VMTransition aJ = rp.getVMAction(vmJ);
                    disallowOverlap(s, aI, aJ);
                }
            }
        }
        return true;
    }

    private void disallowOverlap(Solver s, VMTransition t1, VMTransition t2) {
        Slice dI = t1.getDSlice();
        Slice cJ = t1.getCSlice();

        Slice dJ = t2.getDSlice();
        Slice cI = t2.getCSlice();

        if (dI != null && cJ != null) {
            precedenceIfOverlap(s, dI, cJ);
        }
        //The inverse relation
        if (dJ != null && cI != null) {
            precedenceIfOverlap(s, dJ, cI);
        }
    }

    /**
     * Establish the precedence constraint {@code c.getEnd() <= d.getStart()} if the two slices may overlap.
     */
    private void precedenceIfOverlap(Solver s, Slice d, Slice c) {
        //No need to place the constraints if the slices do not have a chance to overlap
        if (!(c.getHoster().isInstantiated() && !d.getHoster().contains(c.getHoster().getValue()))
                && !(d.getHoster().isInstantiated() && !c.getHoster().contains(d.getHoster().getValue()))
                ) {
            BoolVar eq = new Arithmetic(d.getHoster(), Operator.EQ, c.getHoster()).reif();
            solver.constraints.Constraint leqCstr = new Arithmetic(c.getEnd(), Operator.LE, d.getStart());
            ChocoUtils.postImplies(s, eq, leqCstr);
        }
    }

    private List<IntVar> placementVariables(ReconfigurationProblem rp) {
        List<IntVar> running = new ArrayList<>();
        for (VM vmId : cstr.getInvolvedVMs()) {
            if (rp.getFutureRunningVMs().contains(vmId)) {
                VMTransition a = rp.getVMAction(vmId);
                Slice d = a.getDSlice();
                if (d != null) {
                    running.add(d.getHoster());
                }
            }
        }
        return running;
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
