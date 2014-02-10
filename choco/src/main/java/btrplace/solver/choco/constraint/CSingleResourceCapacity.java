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
import btrplace.model.constraint.SingleResourceCapacity;
import btrplace.model.view.ShareableResource;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.view.CShareableResource;
import solver.Cause;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

import java.util.HashSet;
import java.util.Set;


/**
 * Choco implementation of {@link btrplace.model.constraint.SingleResourceCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CSingleResourceCapacity implements ChocoConstraint {

    private SingleResourceCapacity cstr;

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CSingleResourceCapacity(SingleResourceCapacity c) {
        cstr = c;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        CShareableResource rcm = (CShareableResource) rp.getView(ShareableResource.VIEW_ID_BASE + cstr.getResource());
        if (rcm == null) {
            throw new SolverException(rp.getSourceModel(), "Unable to find a resource mapping for resource '" + cstr.getResource() + "'");
        }
        int amount = cstr.getAmount();
        Solver s = rp.getSolver();
        for (Node n : cstr.getInvolvedNodes()) {
            IntVar v = rcm.getVirtualUsage()[rp.getNode(n)];
            s.post(IntConstraintFactory.arithm(v, "<=", amount));
            //s.post(s.leq(v, amount));

            //Continuous in practice ?
            if (cstr.isContinuous()) {
                if (cstr.isSatisfied(rp.getSourceModel())) {
                    try {
                        v.updateUpperBound(cstr.getAmount(), Cause.Null);
                    } catch (ContradictionException e) {
                        rp.getLogger().error("Unable to restrict to up to {}, the maximum '{}' usage on '{}': ", cstr.getAmount(), rcm.getResourceIdentifier(), n, e.getMessage());
                        return false;
                    }
                } else {
                    rp.getLogger().error("The constraint '{}' must be already satisfied to provide a continuous restriction", cstr);
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
        ShareableResource rc = (ShareableResource) m.getView(ShareableResource.VIEW_ID_BASE + cstr.getResource());
        for (Node n : cstr.getInvolvedNodes()) {
            int remainder = cstr.getAmount();
            for (VM v : map.getRunningVMs(n)) {
                remainder -= rc.getConsumption(v);
                if (remainder < 0) {
                    bad.addAll(map.getRunningVMs(n));
                    break;
                }
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
            return SingleResourceCapacity.class;
        }

        @Override
        public CSingleResourceCapacity build(Constraint cstr) {
            return new CSingleResourceCapacity((SingleResourceCapacity) cstr);
        }
    }
}
