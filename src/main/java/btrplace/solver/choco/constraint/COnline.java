/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.Online;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ActionModel;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import choco.kernel.solver.ContradictionException;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Choco implementation of {@link btrplace.model.constraint.Online}.
 *
 * @author Fabien Hermenier
 */
public class COnline implements ChocoSatConstraint {

    private Online cstr;

    /**
     * Make a new constraint.
     *
     * @param o the {@link SatConstraint} to rely on
     */
    public COnline(Online o) {
        this.cstr = o;
    }

    @Override
    public void inject(ReconfigurationProblem rp) throws SolverException {
        for (UUID nId : cstr.getInvolvedNodes()) {
            int idx = rp.getNode(nId);
            ActionModel m = rp.getNodeActions()[idx];
            try {
                m.getState().setVal(1);
            } catch (ContradictionException e) {
                throw new SolverException(rp.getSourceModel(), "Unable to force '" + nId + "' at getting online");
            }
        }

    }

    @Override
    public Online getAssociatedConstraint() {
        return cstr;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        return new HashSet<UUID>();
    }

    @Override
    public boolean isSatisfied(ReconfigurationPlan plan) {
        throw new UnsupportedOperationException();
    }


    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoSatConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return Online.class;
        }

        @Override
        public COnline build(SatConstraint cstr) {
            return new COnline((Online) cstr);
        }
    }
}
