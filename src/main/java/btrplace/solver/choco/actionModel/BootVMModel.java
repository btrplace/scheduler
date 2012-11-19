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

package btrplace.solver.choco.actionModel;

import btrplace.plan.Action;
import btrplace.plan.SolverException;
import btrplace.plan.action.BootVM;
import btrplace.solver.choco.ActionModel;
import btrplace.solver.choco.DurationEvaluators;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import choco.cp.solver.CPSolver;
import choco.cp.solver.variables.integer.IntDomainVarAddCste;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model an action that boot a VM in the waiting state.
 *
 * @author Fabien Hermenier
 */
public class BootVMModel extends ActionModel {

    public BootVMModel(ReconfigurationProblem rp, UUID e) throws SolverException {
        super(rp, e);
        DurationEvaluators dEvals = rp.getDurationEvaluator();

        CPSolver s = rp.getSolver();
        int d = dEvals.evaluate(BootVM.class, e);
        if (d < 0) {
            throw new SolverException(rp.getSourceModel(), "Unable to evaluate the duration of runVM(" + e + ")");
        }

        start = s.createBoundIntVar("", 0, rp.getEnd().getSup() - d);
        end = new IntDomainVarAddCste(rp.getSolver(), "", getStart(), d);
        duration = s.createIntegerConstant("", d);
        IntDomainVar hoster = s.createEnumIntVar("", 0, rp.getNodes().length - 1);
        IntDomainVar sDuration = s.createBoundIntVar("", d, rp.getEnd().getSup());

        dSlice = new Slice("dSlice(" + e + ")", start, rp.getEnd(), sDuration, hoster);
        rp.getSolver().post(s.eq(dSlice.getEnd(), s.plus(dSlice.getDuration(), dSlice.getStart())));
    }

    @Override
    public List<Action> getResultingActions(ReconfigurationProblem rp) {
        List<Action> l = new ArrayList<Action>(1);
        l.add(new BootVM(getSubject(), rp.getNode(dSlice.getHoster().getVal()), start.getVal(), end.getVal()));
        return l;
    }
}
