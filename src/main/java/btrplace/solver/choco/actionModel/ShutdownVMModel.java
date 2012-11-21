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
import btrplace.plan.action.ShutdownVM;
import btrplace.solver.choco.ActionModel;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.SliceBuilder;
import choco.cp.solver.variables.integer.IntDomainVarAddCste;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model an action that stop a running VM.
 *
 * @author Fabien Hermenier
 */
public class ShutdownVMModel extends ActionModel {

    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the VM managed by the action
     * @throws SolverException if an error occurred
     */
    public ShutdownVMModel(ReconfigurationProblem rp, UUID e) throws SolverException {
        super(rp, e);

        int d = rp.getDurationEvaluator().evaluate(ShutdownVM.class, e);

        duration = rp.getSolver().createIntegerConstant("", d);
        this.cSlice = new SliceBuilder(rp, e).setHoster(rp.getCurrentVMLocation(rp.getVM(e)))
                .setEnd(rp.makeDuration("", d, rp.getEnd().getSup()))
                .setExclusive(false)
                .build();

        end = cSlice.getEnd();
        cost = end;
        start = new IntDomainVarAddCste(rp.getSolver(), "", end, -d);
    }

    @Override
    public List<Action> getResultingActions(ReconfigurationProblem rp) {
        List<Action> l = new ArrayList<Action>();
        l.add(new ShutdownVM(getSubject(),
                rp.getSourceModel().getMapping().getVMLocation(getSubject()),
                start.getVal(),
                end.getVal()));
        return l;
    }
}
