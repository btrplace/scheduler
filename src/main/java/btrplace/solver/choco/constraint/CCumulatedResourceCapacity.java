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

package btrplace.solver.choco.constraint;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.ShareableResource;
import btrplace.model.constraint.CumulatedResourceCapacity;
import btrplace.model.constraint.CumulatedRunningCapacity;
import btrplace.solver.SolverException;
import btrplace.solver.choco.*;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntArrayList;

import java.util.*;

/**
 * Choco implementation of {@link btrplace.model.constraint.CumulatedResourceCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CCumulatedResourceCapacity implements ChocoSatConstraint {

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
        if (cstr.isContinuous()) {
            //The constraint must be already satisfied
            if (!cstr.isSatisfied(rp.getSourceModel()).equals(SatConstraint.Sat.SATISFIED)) {
                rp.getLogger().error("The constraint '{}' must be already satisfied to provide a continuous restriction", cstr);
                return false;
            } else {
                int[] alias = new int[cstr.getInvolvedNodes().size()];
                int i = 0;
                for (UUID n : cstr.getInvolvedNodes()) {
                    alias[i++] = rp.getNode(n);
                }

                TIntArrayList cUse = new TIntArrayList();
                List<IntDomainVar> dUse = new ArrayList<IntDomainVar>();

                ResourceMapping rcm = rp.getResourceMapping(cstr.getResource());
                if (rcm == null) {
                    throw new SolverException(rp.getSourceModel(), "No resource associated to identifier '" + cstr.getResource() + "'");
                }
                for (UUID vmId : rp.getVMs()) {
                    VMActionModel a = rp.getVMAction(vmId);
                    Slice c = a.getCSlice();
                    Slice d = a.getDSlice();
                    if (c != null) {
                        cUse.add(rcm.getSourceResource().get(vmId));
                    }
                    if (d != null) {
                        dUse.add(rcm.getVMsAllocation()[rp.getVM(vmId)]);
                    }
                }
                rp.getAliasedCumulativesBuilder().add(cstr.getAmount(), cUse.toNativeArray(), dUse.toArray(new IntDomainVar[dUse.size()]), alias);
            }
        }
        ResourceMapping rcm = rp.getResourceMapping(cstr.getResource());
        if (rcm == null) {
            throw new SolverException(rp.getSourceModel(), "Unable to find a resource mapping for resource '" + cstr.getResource() + "'");
        }
        List<IntDomainVar> vs = new ArrayList<IntDomainVar>();
        for (UUID u : cstr.getInvolvedNodes()) {
            vs.add(rcm.getVirtualUsage()[rp.getNode(u)]);
        }
        CPSolver s = rp.getSolver();
        s.post(s.leq(CPSolver.sum(vs.toArray(new IntDomainVar[vs.size()])), cstr.getAmount()));
        return true;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        Mapping map = m.getMapping();
        ShareableResource rc = m.getResource(cstr.getResource());
        Set<UUID> bad = new HashSet<UUID>();
        int remainder = cstr.getAmount();
        for (UUID n : cstr.getInvolvedNodes()) {
            for (UUID v : map.getRunningVMs(n)) {
                remainder -= rc.get(v);
                if (remainder < 0) {
                    for (UUID n2 : cstr.getInvolvedNodes()) {
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
    public static class Builder implements ChocoSatConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return CumulatedRunningCapacity.class;
        }

        @Override
        public CCumulatedResourceCapacity build(SatConstraint cstr) {
            return new CCumulatedResourceCapacity((CumulatedResourceCapacity) cstr);
        }
    }
}
