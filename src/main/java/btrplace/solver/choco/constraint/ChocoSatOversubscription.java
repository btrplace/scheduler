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

import btrplace.model.Model;
import btrplace.model.constraint.Oversubscription;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.SolverException;
import btrplace.solver.choco.*;
import btrplace.solver.choco.chocoUtil.BinPacking;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Choco implementation of {@link btrplace.model.SatConstraint}.
 *
 * @author Fabien Hermenier
 */
public class ChocoSatOversubscription implements ChocoSatConstraint {

    private Oversubscription cstr;

    public ChocoSatOversubscription(Oversubscription o) {
        cstr = o;
    }

    @Override
    public void inject(ReconfigurationProblem rp) throws SolverException {

        CPSolver s = rp.getSolver();
        ResourceMapping rcm = rp.getResourceMapping(cstr.getResource());
        if (rcm == null) {
            throw new SolverException(rp.getSourceModel(), "Unable to get the resource mapping '" + cstr.getResource() + "'");
        }
        IntDomainVar[] capa = rcm.getCapacities();
        List<Slice> dSlices = rp.getDSlices();
        IntDomainVar[] usages = new IntDomainVar[dSlices.size()];
        IntDomainVar[] hosters = SliceUtils.extractHosters(dSlices);
        for (int i = 0; i < dSlices.size(); i++) {
            UUID e = dSlices.get(i).getSubject();
            usages[i] = s.createIntegerConstant("", rcm.getUsage()[rp.getVM(e)]);
        }

        s.post(new BinPacking(s.getEnvironment(), capa, usages, hosters));
    }

    @Override
    public Oversubscription getAssociatedConstraint() {
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
