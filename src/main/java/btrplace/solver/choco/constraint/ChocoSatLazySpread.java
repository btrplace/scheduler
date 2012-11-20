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
import btrplace.model.constraint.Spread;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.choco.ChocoConstraintBuilder;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ReconfigurationProblem;
import choco.cp.solver.constraints.global.BoundAllDiff;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.*;

/**
 * An implementation of {@link Spread} that only ensure VMs will not be
 * co-located at the end of the reconfiguration. Temporary overlapping during
 * the reconfiguration is still possible. To prevent for that situation, use {@link ChocoSatContinuousSpread}
 * instead.
 *
 * @author Fabien Hermenier
 */
public class ChocoSatLazySpread implements ChocoSatConstraint {

    private Spread cstr;

    /**
     * The builder associated to that constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return Spread.class;
        }

        @Override
        public ChocoSatLazySpread build(SatConstraint cstr) {
            return new ChocoSatLazySpread((Spread) cstr);
        }
    }

    /**
     * Make a new constraint.
     *
     * @param s the constraint to rely on
     */
    public ChocoSatLazySpread(Spread s) {
        cstr = s;
    }

    @Override
    public void inject(ReconfigurationProblem rp) {
        Set<UUID> onlyRunnings = new HashSet<UUID>();
        Mapping m = rp.getSourceModel().getMapping();
        for (UUID vmId : cstr.getInvolvedVMs()) {
            if (rp.getFutureRunningVMs().contains(vmId) || m.getRunningVMs().contains(vmId)) {
                onlyRunnings.add(vmId);
            }
        }
        Solver s = rp.getSolver();

        if (!onlyRunnings.isEmpty()) {
            s.post(new BoundAllDiff(onlyRunnings.toArray(new IntDomainVar[onlyRunnings.size()]), true));
        }
    }

    @Override
    public Spread getAssociatedConstraint() {
        return cstr;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        Map<UUID, Set<UUID>> spots = new HashMap<UUID, Set<UUID>>();
        Set<UUID> bad = new HashSet<UUID>();
        Mapping map = m.getMapping();
        for (UUID vm : cstr.getInvolvedVMs()) {
            UUID h = map.getVMLocation(vm);
            if (map.getRunningVMs().contains(vm)) {
                if (!spots.containsKey(h)) {
                    spots.put(h, new HashSet<UUID>());
                }
                spots.get(h).add(vm);
            }

        }
        for (Map.Entry<UUID, Set<UUID>> e : spots.entrySet()) {
            if (e.getValue().size() > 1) {
                bad.addAll(e.getValue());
            }
        }
        return bad;
    }

    @Override
    public boolean isSatisfied(ReconfigurationPlan plan) {
        return cstr.isSatisfied(plan.getResult()).equals(SatConstraint.Sat.SATISFIED);
    }
}
