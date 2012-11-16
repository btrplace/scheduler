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

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.SolverException;

/**
 * Created with IntelliJ IDEA.
 * User: fhermeni
 * Date: 15/11/12
 * Time: 13:35
 * To change this template use File | Settings | File Templates.
 */
public class DefaultChocoReconfigurationAlgorithm implements ChocoReconfigurationAlgorithm {

    private SatConstraintMapper cstrMapper;

    private boolean optimize = false;

    private int timeLimit;

    public DefaultChocoReconfigurationAlgorithm() {
        cstrMapper = new SatConstraintMapper();
    }
    @Override
    public void doOptimize(boolean b) {
        this.optimize = b;
    }

    @Override
    public boolean doOptimize() {
        return this.optimize;
    }

    @Override
    public void setTimeLimit(int t) {
        timeLimit = t;
    }

    @Override
    public int getTimeLimit() {
        return timeLimit;
    }

    @Override
    public ReconfigurationPlan solve(Model i) throws SolverException {

        for (SatConstraint cstr : i.getConstraints()) {
            ChocoConstraintBuilder ccstrb = cstrMapper.get(cstr);
            if (ccstrb == null) {
                throw new SolverException(i, "Unable to map constraint '"+ cstr.getClass().getSimpleName() + "'");
            }
            ChocoConstraint ccstr = ccstrb.build(cstr);
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SatConstraintMapper getSatConstraintMapper() {
        return cstrMapper;
    }
}
