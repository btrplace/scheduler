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
import btrplace.solver.choco.*;
import btrplace.solver.choco.chocoUtil.ChocoUtils;
import choco.cp.solver.constraints.reified.ReifiedFactory;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Continuous implementation of {@link Spread}.
 *
 * @author Fabien Hermenier
 */
public class ChocoSatContinuousSpread implements ChocoSatConstraint {

    private Spread cstr;

    public static class ChocoContinuousSpreadBuilder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return Spread.class;
        }

        @Override
        public ChocoSatContinuousSpread build(SatConstraint cstr) {
            return new ChocoSatContinuousSpread((Spread) cstr);
        }
    }

    public ChocoSatContinuousSpread(Spread s) {
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
            //The lazy spread implementation for the placement
            new ChocoSatLazySpread(cstr).inject(rp);
            UUID[] vms = onlyRunnings.toArray(new UUID[onlyRunnings.size()]);
            for (int i = 0; i < vms.length; i++) {
                UUID vm = vms[i];
                ActionModel aI = rp.getVMAction(rp.getVM(vm));
                for (int j = 0; j < i; j++) {
                    UUID vmJ = vms[j];
                    ActionModel aJ = rp.getVMAction(rp.getVM(vm));
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

    @Override
    public Spread getAssociatedConstraint() {
        return cstr;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSatisfied(ReconfigurationPlan plan) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
