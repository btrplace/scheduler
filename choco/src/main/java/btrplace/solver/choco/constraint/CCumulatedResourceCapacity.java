/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.Constraint;
import btrplace.model.constraint.CumulatedResourceCapacity;
import btrplace.model.view.ShareableResource;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.actionModel.VMActionModel;
import btrplace.solver.choco.view.CShareableResource;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.variables.IntVar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Choco implementation of {@link btrplace.model.constraint.CumulatedResourceCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CCumulatedResourceCapacity implements ChocoConstraint {

    private CumulatedResourceCapacity cstr;

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CCumulatedResourceCapacity(CumulatedResourceCapacity c) {
        cstr = c;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {

        CShareableResource rcm = (CShareableResource) rp.getView(ShareableResource.VIEW_ID_BASE + cstr.getResource());
        if (rcm == null) {
            throw new SolverException(rp.getSourceModel(), "No resource associated to identifier '" + cstr.getResource() + "'");
        }

        if (cstr.isContinuous()) {
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
                    VMActionModel a = rp.getVMAction(vmId);
                    Slice c = a.getCSlice();
                    Slice d = a.getDSlice();
                    if (c != null) {
                        cUse.add(rcm.getSourceResource().getConsumption(vmId));
                    }
                    if (d != null) {
                        dUse.add(rcm.getVMsAllocation()[rp.getVM(vmId)]);
                    }
                }
                rp.getAliasedCumulativesBuilder().add(cstr.getAmount(), cUse.toArray(), dUse.toArray(new IntVar[dUse.size()]), alias);
            }
        }
        List<IntVar> vs = new ArrayList<>();
        for (Node u : cstr.getInvolvedNodes()) {
            vs.add(rcm.getVirtualUsage()[rp.getNode(u)]);
        }
        Solver s = rp.getSolver();
        s.post(s.leq(Solver.sum(vs.toArray(new IntVar[vs.size()])), cstr.getAmount()));
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
            return CumulatedResourceCapacity.class;
        }

        @Override
        public CCumulatedResourceCapacity build(Constraint cstr) {
            return new CCumulatedResourceCapacity((CumulatedResourceCapacity) cstr);
        }
    }
}
