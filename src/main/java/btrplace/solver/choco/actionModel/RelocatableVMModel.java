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
import btrplace.plan.action.MigrateVM;
import btrplace.solver.choco.ActionModel;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceUtils;
import btrplace.solver.choco.chocoUtil.FastIFFEq;
import btrplace.solver.choco.chocoUtil.FastImpliesEq;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.TimesXYZ;
import choco.cp.solver.constraints.reified.ReifiedFactory;
import choco.cp.solver.variables.integer.BoolVarNot;
import choco.cp.solver.variables.integer.BooleanVarImpl;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model an action that allow a VM to be migrated if necessary.
 *
 * @author Fabien Hermenier
 */
public class RelocatableVMModel extends ActionModel {

    public RelocatableVMModel(ReconfigurationProblem rp, UUID e) throws SolverException {
        super(rp, e);

        int d = rp.getDurationEvaluator().evaluate(MigrateVM.class, e);

        CPSolver s = rp.getSolver();
        this.cost = rp.makeDuration("");
        duration = s.createEnumIntVar("", new int[]{0, d});
        IntDomainVar cDuration = rp.makeDuration("");
        cSlice = new Slice("", rp.getStart(), cDuration, cDuration, rp.makeCurrentHost("", e));

        IntDomainVar dDuration = rp.makeDuration("");
        dSlice = new Slice("", dDuration, rp.getEnd(), null, rp.makeHostVariable(""));
        IntDomainVar move = s.createBooleanVar("");
        s.post(ReifiedFactory.builder(move, s.neq(cSlice.getHoster(), dSlice.getHoster()), s));

        IntDomainVar stay = new BoolVarNot(s, "", (BooleanVarImpl) move);

        s.post(new TimesXYZ(move, cSlice.getEnd(), cost));

        s.post(new FastIFFEq(stay, duration, 0));

        boolean increase = false;
        if (!increase) {
            s.post(new FastImpliesEq(stay, cSlice.getDuration(), 0));
        } else {
            s.post(new FastImpliesEq(stay, dSlice.getDuration(), 0));
        }
        SliceUtils.linkMoments(rp, dSlice);
        SliceUtils.linkMoments(rp, cSlice);
        s.post(s.leq(duration, cSlice.getDuration()));
        s.post(s.leq(duration, dSlice.getDuration()));
        s.post(s.eq(this.getEnd(), s.plus(this.getStart(), duration)));

        //TODO: What about the exlusive dSlice stuff ?
        s.post(s.leq(cSlice.getDuration(), rp.getEnd()));
        s.post(s.leq(dSlice.getDuration(), rp.getEnd()));
    }

    @Override
    public List<Action> getResultingActions(ReconfigurationProblem rp) {
        return new ArrayList<Action>();
    }
}
