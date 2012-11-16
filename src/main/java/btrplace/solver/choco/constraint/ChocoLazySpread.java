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
import btrplace.model.constraint.Spread;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.choco.ChocoConstraint;
import btrplace.solver.choco.ChocoConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;

import java.util.Set;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: fhermeni
 * Date: 15/11/12
 * Time: 16:49
 * To change this template use File | Settings | File Templates.
 */
public class ChocoLazySpread implements ChocoConstraint {

    public static class ChocoContinuousSpreadBuilder implements ChocoConstraintBuilder {
        @Override
        public Class getKey() {
            return Spread.class;
        }

        @Override
        public ChocoConstraint build(SatConstraint cstr) {
            return new ChocoLazySpread((Spread) cstr);
        }
    }

    public ChocoLazySpread(Spread s) {

    }
    @Override
    public void inject(ReconfigurationProblem rp) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SatConstraint getAssociatedConstraint() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSatisfied(ReconfigurationPlan plan) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
