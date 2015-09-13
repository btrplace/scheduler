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

import gnu.trove.list.array.TIntArrayList;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.ResourceCapacity;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.btrplace.scheduler.choco.view.AliasedCumulatives;
import org.btrplace.scheduler.choco.view.CShareableResource;
import org.btrplace.scheduler.choco.view.ChocoView;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Choco implementation of {@link org.btrplace.model.constraint.ResourceCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CResourceCapacity implements ChocoConstraint {

    private ResourceCapacity cstr;

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CResourceCapacity(ResourceCapacity c) {
        cstr = c;
    }

    private boolean injectWithSingleNode(CShareableResource rcm, ReconfigurationProblem rp) {
        int amount = cstr.getAmount();
        Solver s = rp.getSolver();
        Node n = cstr.getInvolvedNodes().iterator().next();
        int nIdx = rp.getNode(n);
        IntVar v = rcm.getVirtualUsage()[nIdx];
        s.post(IntConstraintFactory.arithm(v, "<=", amount));

        //Continuous in practice ?
        if (cstr.isContinuous()) {
            if (cstr.isSatisfied(rp.getSourceModel())) {
                try {
                    v.updateUpperBound(cstr.getAmount(), Cause.Null);
                } catch (ContradictionException e) {
                    rp.getLogger().error("Unable to restrict to up to {}, the maximum '{}' usage on '{}': ", cstr.getAmount(), rcm.getResourceIdentifier(), n, e.getMessage());
                    return false;
                }
            } else {
                rp.getLogger().error("The constraint '{}' must be already satisfied to provide a continuous restriction", cstr);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SchedulerException {

        CShareableResource rcm = (CShareableResource) rp.getView(ShareableResource.VIEW_ID_BASE + cstr.getResource());
        if (rcm == null) {
            throw new SchedulerException(rp.getSourceModel(), "No resource associated to identifier '" + cstr.getResource() + "'");
        }

        if (cstr.getInvolvedNodes().size() == 1) {
            return injectWithSingleNode(rcm, rp);
        }

        if (cstr.isContinuous() && !injectContinuous(rp, rcm)) {
            return false;
        }

        List<IntVar> vs = new ArrayList<>();
        for (Node u : cstr.getInvolvedNodes()) {
            vs.add(rcm.getVirtualUsage()[rp.getNode(u)]);
        }
        Solver s = rp.getSolver();
        IntVar mySum = VariableFactory.bounded(rp.makeVarLabel("usage(", rcm.getIdentifier(), ")"), 0, Integer.MAX_VALUE / 100, s);
        s.post(IntConstraintFactory.sum(vs.toArray(new IntVar[vs.size()]), mySum));
        s.post(IntConstraintFactory.arithm(mySum, "<=", cstr.getAmount()));
        return true;
    }

    private boolean injectContinuous(ReconfigurationProblem rp, CShareableResource rcm) throws SchedulerException {
        //The constraint must be already satisfied
        if (!cstr.isSatisfied(rp.getSourceModel())) {
            rp.getLogger().error("The constraint '{}' must be already satisfied to provide a continuous restriction", cstr);
            return false;
        } else {
            int[] alias = new int[cstr.getInvolvedNodes().size()];
            int i = 0;
            for (Node n : cstr.getInvolvedNodes()) {
                alias[i++] = rp.getNode(n);
            }

            TIntArrayList cUse = new TIntArrayList();
            List<IntVar> dUse = new ArrayList<>();

            for (VM vmId : rp.getVMs()) {
                VMTransition a = rp.getVMAction(vmId);
                Slice c = a.getCSlice();
                Slice d = a.getDSlice();
                if (c != null) {
                    cUse.add(rcm.getSourceResource().getConsumption(vmId));
                }
                if (d != null) {
                    dUse.add(rcm.getVMsAllocation()[rp.getVM(vmId)]);
                }
            }
            ChocoView v = rp.getView(AliasedCumulatives.VIEW_ID);
            if (v == null) {
                throw new SchedulerException(rp.getSourceModel(), "View '" + AliasedCumulatives.VIEW_ID + "' is required but missing");
            }
            ((AliasedCumulatives) v).addDim(cstr.getAmount(), cUse.toArray(), dUse.toArray(new IntVar[dUse.size()]), alias);
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        Mapping map = m.getMapping();
        ShareableResource rc = (ShareableResource) m.getView(ShareableResource.VIEW_ID_BASE + cstr.getResource());
        if (rc == null) {
            return map.getRunningVMs(cstr.getInvolvedNodes());
        }
        Set<VM> bad = new HashSet<>();
        int remainder = cstr.getAmount();
        for (Node n : cstr.getInvolvedNodes()) {
            for (VM v : map.getRunningVMs(n)) {
                remainder -= rc.getConsumption(v);
                if (remainder < 0) {
                    for (Node n2 : cstr.getInvolvedNodes()) {
                        bad.addAll(map.getRunningVMs(n2));
                    }
                    return bad;
                }
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
            return ResourceCapacity.class;
        }

        @Override
        public CResourceCapacity build(Constraint c) {
            return new CResourceCapacity((ResourceCapacity) c);
        }
    }
}
