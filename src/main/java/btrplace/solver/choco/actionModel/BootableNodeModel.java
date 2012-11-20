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
import btrplace.plan.action.BootNode;
import btrplace.solver.choco.ActionModel;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceUtils;
import btrplace.solver.choco.chocoUtil.FastImpliesEq;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.reified.ReifiedFactory;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model an action that allow a node to be booted if necessary.
 *
 * @author Fabien Hermenier
 */
public class BootableNodeModel extends ActionModel {

    /**
     * Make a new model.
     *
     * @param rp  the RP to use as a basis.
     * @param nId the node managed by the action
     * @throws SolverException if an error occurred
     */
    public BootableNodeModel(ReconfigurationProblem rp, UUID nId) throws SolverException {
        super(rp, nId);
        state = rp.getSolver().createBooleanVar("");

        int nIdx = rp.getNode(nId);

        //A c-slice that consume all the resources to represent the boot process.
        int d = rp.getDurationEvaluator().evaluate(BootNode.class, nId);
        CPSolver s = rp.getSolver();
        cSlice = new Slice("", nId, rp.makeDuration(""), rp.makeDuration("", d, rp.getEnd().getSup()), rp.makeDuration("", d, rp.getEnd().getSup()), rp.makeCurrentNode("", nId), s.createIntegerConstant("", 0));
        cost = s.createEnumIntVar("cost(" + toString() + ")", new int[]{0, d});
        state = s.createBooleanVar("");

        //The cost equals the estimated duration <=> the node is booted. Otherwise it will equals 0
        s.post(new FastImpliesEq(state, cost, 0));

        /**
         * used denotes whether or not the node is used, \ie it host running VMs
         * In practice, we consider that if some memory are used, then the node is used
         * (it avoids to use an Occurrence constraint)
         */
        IntDomainVar used = s.createBooleanVar("");
        s.post(ReifiedFactory.builder(used, s.neq(rp.getVMsCountOnNodes()[nIdx], 0), s));
        s.post(new FastImpliesEq(used, state, 1));

        s.post(s.eq(cSlice.getEnd(), cost));


        SliceUtils.linkMoments(rp, cSlice);
    }

    @Override
    public List<Action> getResultingActions(ReconfigurationProblem rp) {
        List<Action> a = new ArrayList<Action>();
        if (start.getVal() == 1) {
            a.add(new BootNode(getSubject(), start.getVal(), end.getVal()));
        }
        return a;
    }
}
