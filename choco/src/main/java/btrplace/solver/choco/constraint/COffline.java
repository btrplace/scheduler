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
import btrplace.model.constraint.Offline;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.transition.Transition;
import btrplace.solver.choco.transition.VMTransition;
import solver.Cause;
import solver.exception.ContradictionException;

import java.util.Set;


/**
 * Choco implementation of {@link Offline}.
 *
 * @author Fabien Hermenier
 */
public class COffline implements ChocoConstraint {

    private Offline cstr;

    /**
     * Make a new constraint.
     *
     * @param o the {@link Offline} constraint to rely on
     */
    public COffline(Offline o) {
        this.cstr = o;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        if (cstr.isContinuous() && !cstr.getChecker().startsWith(rp.getSourceModel())) {
            rp.getLogger().error("Constraint {} is not satisfied initially", cstr);
            return false;
        }

        Node nId = cstr.getInvolvedNodes().iterator().next();
        int id = rp.getNode(nId);
        Transition m = rp.getNodeAction(nId);
        try {
            m.getState().instantiateTo(0, Cause.Null);
            if (rp.getSourceModel().getMapping().isOffline(nId)) {
                m.getStart().instantiateTo(0, Cause.Null);
            }
        } catch (ContradictionException ex) {
            rp.getLogger().error("Unable to force node '{}' at being offline: {}", nId);
            return false;
        }
        for (VMTransition am : rp.getVMActions()) {
            Slice s = am.getDSlice();
            if (s != null) {
                try {
                    s.getHoster().removeValue(id, Cause.Null);
                } catch (ContradictionException e) {
                    rp.getLogger().error("Unable to remove VM '{}' of node {}: {}", am.getVM(), nId, e.getMessage());
                }
                //rp.getSolver().post(new solver.constraints.Arithmetic(s.getHoster(), Operator.NQ, id));
            }
        }
        return true;

    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        Mapping mapping = m.getMapping();
        return mapping.getRunningVMs(cstr.getInvolvedNodes().iterator().next());
    }

    @Override
    public String toString() {
        return cstr.toString();
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return Offline.class;
        }

        @Override
        public COffline build(Constraint c) {
            return new COffline((Offline) c);
        }
    }
}
