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
import btrplace.model.constraint.SingleRunningCapacity;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import solver.Cause;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

import java.util.HashSet;
import java.util.Set;


/**
 * Choco implementation of {@link btrplace.model.constraint.SingleRunningCapacity}.
 * <p/>
 * The implementation support the continuous or the discrete restriction.
 * If the continuous restriction is set, it is activated only if the current model does not violate the constraint.
 * Otherwise, there is no solution.
 *
 * @author Fabien Hermenier
 */
public class CSingleRunningCapacity implements ChocoConstraint {

    private SingleRunningCapacity cstr;

    /**
     * Make a new constraint.
     *
     * @param c the sat constraint to rely on
     */
    public CSingleRunningCapacity(SingleRunningCapacity c) {
        cstr = c;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        Solver s = rp.getSolver();
        for (Node u : cstr.getInvolvedNodes()) {
            IntVar v = rp.getNbRunningVMs()[rp.getNode(u)];
            s.post(IntConstraintFactory.arithm(v, "<=", cstr.getAmount()));
            //s.post(s.leq(v, cstr.getAmount()));

            //Continuous in practice ?
            if (cstr.isContinuous() && cstr.isSatisfied(rp.getSourceModel())) {
                try {
                    v.updateUpperBound(cstr.getAmount(), Cause.Null);
                } catch (ContradictionException e) {
                    rp.getLogger().error("Unable to restrict to up to {}, the maximum amount of VMs on '{}': ", cstr.getAmount(), u, e.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        Mapping map = m.getMapping();
        Set<VM> bad = new HashSet<>();
        for (Node n : cstr.getInvolvedNodes()) {
            if (map.getRunningVMs(n).size() > cstr.getAmount()) {
                bad.addAll(map.getRunningVMs(n));
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
            return SingleRunningCapacity.class;
        }

        @Override
        public CSingleRunningCapacity build(Constraint cstr) {
            return new CSingleRunningCapacity((SingleRunningCapacity) cstr);
        }
    }
}
