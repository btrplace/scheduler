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
import btrplace.model.VM;
import btrplace.model.constraint.Constraint;
import btrplace.model.constraint.Ready;
import btrplace.solver.choco.ReconfigurationProblem;

import java.util.Collections;
import java.util.Set;


/**
 * Naive implementation of {@link btrplace.model.constraint.Ready}.
 * This constraint is just a stub to be consistent with the model. It does not state any constraint
 * as the state has already been expressed inside {@link btrplace.solver.choco.ReconfigurationProblem}.
 *
 * @author Fabien Hermenier
 */
public class CReady implements ChocoConstraint {

    private Ready cstr;

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CReady(Ready c) {
        cstr = c;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) {
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        VM v = cstr.getInvolvedVMs().iterator().next();
        Mapping map = m.getMapping();
        if (!map.isReady(v)) {
            return Collections.singleton(v);
        }
        return Collections.emptySet();
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
            return Ready.class;
        }

        @Override
        public CReady build(Constraint c) {
            return new CReady((Ready) c);
        }
    }
}
