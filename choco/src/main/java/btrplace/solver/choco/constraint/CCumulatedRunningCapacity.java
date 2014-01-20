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
import btrplace.model.constraint.CumulatedRunningCapacity;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import solver.Solver;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.*;

/**
 * Choco implementation of {@link CumulatedRunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CCumulatedRunningCapacity implements ChocoConstraint {

    private CumulatedRunningCapacity cstr;

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CCumulatedRunningCapacity(CumulatedRunningCapacity c) {
        cstr = c;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        Solver s = rp.getSolver();
        if (cstr.isContinuous()) {
            //The constraint must be already satisfied
            if (!cstr.isSatisfied(rp.getSourceModel())) {
                rp.getLogger().error("The constraint '{}' must be already satisfied to provide a continuous restriction", cstr);
                return false;
            } else {
                int[] alias = new int[cstr.getInvolvedNodes().size()];
                int i = 0;
                for (Node n : cstr.getInvolvedNodes()) {
                    alias[i++] = rp.getNode(n);
                }
                int nbRunnings = 0;
                for (Node n : rp.getSourceModel().getMapping().getOnlineNodes()) {
                    nbRunnings += rp.getSourceModel().getMapping().getRunningVMs(n).size();
                }
                int[] cUse = new int[nbRunnings];
                IntVar[] dUse = new IntVar[rp.getFutureRunningVMs().size()];
                Arrays.fill(cUse, 1);
                Arrays.fill(dUse, VariableFactory.one(rp.getSolver()));
                rp.getAliasedCumulativesBuilder().add(cstr.getAmount(), cUse, dUse, alias);
            }
        }
        List<IntVar> vs = new ArrayList<>();
        for (Node u : cstr.getInvolvedNodes()) {
            vs.add(rp.getNbRunningVMs()[rp.getNode(u)]);
        }
        //Try to get a lower bound
        //basically, we count 1 per VM necessarily in the set of nodes
        //if involved nodes == all the nodes, then sum == nb of running VMs
        IntExp on = Solver.sum(vs.toArray(new IntVar[vs.size()]));
        if (cstr.getInvolvedNodes().equals(rp.getSourceModel().getMapping().getAllNodes())) {
            s.post(s.eq(on, rp.getFutureRunningVMs().size()));
        }
        s.post(s.leq(on, cstr.getAmount()));
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        Mapping map = m.getMapping();
        Set<VM> bad = new HashSet<>();
        int remainder = cstr.getAmount();
        for (Node n : cstr.getInvolvedNodes()) {
            remainder -= map.getRunningVMs(n).size();
            if (remainder < 0) {
                for (Node n2 : cstr.getInvolvedNodes()) {
                    bad.addAll(map.getRunningVMs(n2));
                }
                return bad;
            }
        }
        return bad;
    }

    @Override
    public String toString() {
        return cstr.toString();
    }

    /**
     * The builder associated to that constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return CumulatedRunningCapacity.class;
        }

        @Override
        public CCumulatedRunningCapacity build(Constraint cstr) {
            return new CCumulatedRunningCapacity((CumulatedRunningCapacity) cstr);
        }
    }
}
