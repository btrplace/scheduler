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

package btrplace.solver.choco;

import btrplace.model.SatConstraint;
import btrplace.model.constraint.Spread;
import btrplace.solver.choco.constraint.ChocoContinuousSpread;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: fhermeni
 * Date: 15/11/12
 * Time: 21:44
 * To change this template use File | Settings | File Templates.
 */
public class SatConstraintMapper {

    private Map<Class, ChocoConstraintBuilder> builders;

    public SatConstraintMapper() {
        builders = new HashMap<Class, ChocoConstraintBuilder>();

        builders.put(Spread.class, new ChocoContinuousSpread.ChocoContinuousSpreadBuilder());
    }

    public boolean register(ChocoConstraintBuilder ccb) {
        return builders.put(ccb.getClass(), ccb) != null;
    }

    public boolean unregister(Class c) {
        return builders.remove(c) != null;
    }

    public ChocoConstraintBuilder get(SatConstraint c) {
        return builders.get(c);
    }
}
