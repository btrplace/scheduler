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
import btrplace.plan.action.ShutdownNode;
import btrplace.solver.choco.ActionModel;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.SliceBuilder;
import btrplace.solver.choco.chocoUtil.FastIFFEq;
import btrplace.solver.choco.chocoUtil.FastImpliesEq;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.TimesXYZ;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model an action that allow a node to boot if necessary.
 *
 * @author Fabien Hermenier
 */
public class ShutdownableNodeModel extends ActionModel {

    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the node managed by the action
     * @throws SolverException if an error occurred
     */
    public ShutdownableNodeModel(ReconfigurationProblem rp, UUID e) throws SolverException {
        super(rp, e);
        state = rp.getSolver().createBooleanVar(rp.makeVarLabel("shutdownNode_state(" + e + ")"));

        CPSolver s = rp.getSolver();
        int d = rp.getDurationEvaluator().evaluate(ShutdownNode.class, e);
        //Duration is either 0 (no shutdown) or 'd' (shutdown)
        duration = s.createEnumIntVar("", new int[]{0, d});


        this.dSlice = new SliceBuilder(rp, e)
                .setStart(rp.makeDuration(rp.makeVarLabel("slice_start(" + e + ")")))
                .setHoster(rp.getNode(e))
                .build();

        end = rp.makeDuration(rp.makeVarLabel("shutdownNode_end(" + e + ")"));

        start = dSlice.getStart();
        s.post(s.eq(end, s.plus(start, duration)));
        //The future state is uncertain yet

        IntDomainVar isOffline = dSlice.isExclusive(); //offline means there will be an exclusive d-Slice
        s.post(s.neq(isOffline, state)); //Cannot rely on BoolVarNot cause it is not compatible with the eq() below
        // Duration necessarily < end of the duration of the reconfiguration process.
        s.post(s.leq(duration, rp.getEnd()));

        /**
         * If it is state to shutdown the node, then the duration of the dSlice is not null
         */
        s.post(new FastIFFEq(state, duration, 0)); //Stay online <-> duration = 0

        s.post(new FastImpliesEq(isOffline, rp.getVMsCountOnNodes()[rp.getNode(e)], 0)); //Packing stuff; isOffline -> mem == 0

        cost = rp.makeDuration(rp.makeVarLabel("shutdownNode_cost(" + e + ")"));
        s.post(new TimesXYZ(end, isOffline, cost));


        //The end of the action is 'd' seconds after starting the d-slice
    }


    @Override
    public List<Action> getResultingActions(ReconfigurationProblem rp) {
        List<Action> a = new ArrayList<Action>();
        if (state.getVal() == 0) {
            a.add(new ShutdownNode(getSubject(), start.getVal(), end.getVal()));
        }
        return a;
    }
}
