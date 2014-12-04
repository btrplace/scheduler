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

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.RunningCapacity;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.view.AliasedCumulatives;
import org.btrplace.scheduler.choco.view.ChocoView;
import org.btrplace.scheduler.choco.view.Cumulatives;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import java.util.*;

/**
 * Choco implementation of {@link org.btrplace.model.constraint.RunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CRunningCapacity implements ChocoConstraint {

    private RunningCapacity cstr;

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CRunningCapacity(RunningCapacity c) {
        cstr = c;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SchedulerException {
        Solver s = rp.getSolver();
        if (cstr.getInvolvedVMs().size() == 1) {
            return filterWithSingleNode(rp);
        }
        if (cstr.isContinuous() && !injectContinuous(rp)) {
            return false;
        }
        List<IntVar> vs = new ArrayList<>();
        for (Node u : cstr.getInvolvedNodes()) {
            vs.add(rp.getNbRunningVMs()[rp.getNode(u)]);
        }
        //Try to get a lower bound
        //basically, we count 1 per VM necessarily in the set of nodes
        //if involved nodes == all the nodes, then sum == nb of running VMs
        IntVar mySum = VariableFactory.bounded(rp.makeVarLabel("nbRunning"), 0, rp.getFutureRunningVMs().size(), rp.getSolver());
        s.post(IntConstraintFactory.sum(vs.toArray(new IntVar[vs.size()]), mySum));
        s.post(IntConstraintFactory.arithm(mySum, "<=", cstr.getAmount()));

        if (cstr.getInvolvedNodes().equals(rp.getSourceModel().getMapping().getAllNodes())) {
            s.post(IntConstraintFactory.arithm(mySum, "=", rp.getFutureRunningVMs().size()));
        }
        return true;
    }

    private boolean injectContinuous(ReconfigurationProblem rp) throws SchedulerException {
        //The constraint must be already satisfied
        if (!cstr.isSatisfied(rp.getSourceModel())) {
            rp.getLogger().error("The constraint '{}' must be already satisfied to provide a continuous restriction", cstr);
            return false;
        }
        int[] alias = new int[cstr.getInvolvedNodes().size()];
        int i = 0;
        for (Node n : cstr.getInvolvedNodes()) {
            alias[i++] = rp.getNode(n);
        }
        int nbRunning = 0;
        for (Node n : rp.getSourceModel().getMapping().getOnlineNodes()) {
            nbRunning += rp.getSourceModel().getMapping().getRunningVMs(n).size();
        }
        int[] cUse = new int[nbRunning];
        IntVar[] dUse = new IntVar[rp.getFutureRunningVMs().size()];
        Arrays.fill(cUse, 1);
        Arrays.fill(dUse, VariableFactory.one(rp.getSolver()));

        ChocoView v = rp.getView(AliasedCumulatives.VIEW_ID);
        if (v == null) {
            throw new SchedulerException(rp.getSourceModel(), "View '" + Cumulatives.VIEW_ID + "' is required but missing");
        }
        ((AliasedCumulatives) v).addDim(cstr.getAmount(), cUse, dUse, alias);
        return true;
    }

    private boolean filterWithSingleNode(ReconfigurationProblem rp) {
        Node n = cstr.getInvolvedNodes().iterator().next();
        IntVar v = rp.getNbRunningVMs()[rp.getNode(n)];
        Solver s = rp.getSolver();
        s.post(IntConstraintFactory.arithm(v, "<=", cstr.getAmount()));

        //Continuous in practice ?
        if (cstr.isContinuous() && cstr.isSatisfied(rp.getSourceModel())) {
            try {
                v.updateUpperBound(cstr.getAmount(), Cause.Null);
            } catch (ContradictionException e) {
                rp.getLogger().error("Unable to cap the amount of VMs on '{}' to {}, : ", n, cstr.getAmount(), e.getMessage());
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        Mapping map = m.getMapping();
        Set<VM> bad = new HashSet<>();
        int remainder = cstr.getAmount();
        for (Node n : cstr.getInvolvedNodes()) {
            remainder -= map.getRunningVMs(n).size();
            if (remainder < 0) {
                for (Node n2 : cstr.getInvolvedNodes()) {
                    bad.addAll(map.getRunningVMs(n2));
                }
                return bad;
            }
        }
        return bad;
    }

    @Override
    public String toString() {
        return cstr.toString();
    }

    /**
     * The builder associated to that constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return RunningCapacity.class;
        }

        @Override
        public CRunningCapacity build(Constraint c) {
            return new CRunningCapacity((RunningCapacity) c);
        }
    }
}
