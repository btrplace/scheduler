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
import btrplace.solver.choco.*;
import btrplace.solver.choco.chocoUtil.ChocoUtils;
import choco.cp.solver.constraints.global.BoundAllDiff;
import choco.cp.solver.constraints.reified.ReifiedFactory;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.*;

/**
 * Continuous implementation of {@link Spread}.
 *
 * @author Fabien Hermenier
 */
public class CSpread implements ChocoSatConstraint {

    private Spread cstr;

    /**
     * Make a new constraint.
     *
     * @param s the constraint to rely one
     */
    public CSpread(Spread s) {
        cstr = s;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) {

        List<IntDomainVar> onlyRunnings = new ArrayList<IntDomainVar>();
        for (UUID vmId : cstr.getInvolvedVMs()) {
            if (rp.getFutureRunningVMs().contains(vmId)) {
                VMActionModel a = rp.getVMAction(vmId);
                Slice d = a.getDSlice();
                if (d != null) {
                    onlyRunnings.add(d.getHoster());
                }
            }
        }
        Solver s = rp.getSolver();
        if (!onlyRunnings.isEmpty()) {
            //The lazy spread implementation for the placement
            s.post(new BoundAllDiff(onlyRunnings.toArray(new IntDomainVar[onlyRunnings.size()]), true));

            if (cstr.isContinuous()) {
                UUID[] vms = new UUID[onlyRunnings.size()];
                int x = 0;
                for (UUID vm : cstr.getInvolvedVMs()) {
                    if (rp.getFutureRunningVMs().contains(vm)) {
                        vms[x++] = vm;
                    }
                }
                for (int i = 0; i < vms.length; i++) {
                    UUID vm = vms[i];
                    VMActionModel aI = rp.getVMAction(vm);
                    for (int j = 0; j < i; j++) {
                        UUID vmJ = vms[j];
                        VMActionModel aJ = rp.getVMAction(vmJ);
                        Slice d = aI.getDSlice();
                        Slice c = aJ.getCSlice();
                        if (d != null && c != null) {
                            //No need to place the constraints if the slices do not have a chance to overlap
                            if (!(c.getHoster().isInstantiated() && !d.getHoster().canBeInstantiatedTo(c.getHoster().getVal()))
                                    && !(d.getHoster().isInstantiated() && !c.getHoster().canBeInstantiatedTo(d.getHoster().getVal()))
                                    ) {
                                IntDomainVar eq = rp.getSolver().createBooleanVar("eq");
                                s.post(ReifiedFactory.builder(eq, s.eq(d.getHoster(), c.getHoster()), s));
                                ChocoUtils.postImplies(s, eq, s.leq(c.getEnd(), d.getStart()));
                            }
                        }

                        //The inverse relation
                        d = aJ.getDSlice();
                        c = aI.getCSlice();

                        if (d != null && c != null) {
                            //No need to place the constraints if the slices do not have a chance to overlap
                            if (!(c.getHoster().isInstantiated() && !d.getHoster().canBeInstantiatedTo(c.getHoster().getVal()))
                                    && !(d.getHoster().isInstantiated() && !c.getHoster().canBeInstantiatedTo(d.getHoster().getVal()))
                                    ) {
                                IntDomainVar eq = s.createBooleanVar("eq");
                                s.post(ReifiedFactory.builder(eq, s.eq(d.getHoster(), c.getHoster()), s));
                                ChocoUtils.postImplies(s, eq, s.leq(c.getEnd(), d.getStart()));
                            }
                        }
                    }
                }
            }
        }
        return true;
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
    public String toString() {
        return cstr.toString();
    }

    /**
     * The builder associated to the constraint.
     */
    public static class Builder implements ChocoSatConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return Spread.class;
        }

        @Override
        public CSpread build(SatConstraint cstr) {
            return new CSpread((Spread) cstr);
        }
    }
}
