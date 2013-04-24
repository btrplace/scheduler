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
import btrplace.model.constraint.Lonely;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.VMActionModel;
import btrplace.solver.choco.chocoUtil.Disjoint;
import btrplace.solver.choco.chocoUtil.Precedences;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntArrayList;

import java.util.*;

/**
 * Choco implementation of {@link btrplace.model.constraint.Lonely}.
 *
 * @author Fabien Hermenier
 */
public class CLonely implements ChocoSatConstraint {

    private Lonely cstr;

    /**
     * Make a new constraint.
     *
     * @param c the lonely constraint to rely on
     */
    public CLonely(Lonely c) {
        this.cstr = c;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        //Remove non future-running VMs
        List<IntDomainVar> myHosts = new ArrayList<>();
        List<IntDomainVar> otherHosts = new ArrayList<>();
        Collection<UUID> vms = new HashSet<>();
        Set<UUID> otherVMs = new HashSet<>();
        for (UUID vm : rp.getFutureRunningVMs()) {
            IntDomainVar host = rp.getVMAction(vm).getDSlice().getHoster();
            if (cstr.getInvolvedVMs().contains(vm)) {
                myHosts.add(host);
                vms.add(vm);
            } else {
                otherHosts.add(host);
                otherVMs.add(vm);
            }
        }
        //Link the assignment variables with the set
        CPSolver s = rp.getSolver();
        s.post(new Disjoint(s.getEnvironment(), myHosts.toArray(new IntDomainVar[myHosts.size()]),
                otherHosts.toArray(new IntDomainVar[otherHosts.size()]),
                rp.getNodes().length));

        if (cstr.isContinuous()) {
            //Get the position of all the others c-slices and their associated end moment
            TIntArrayList otherPos = new TIntArrayList();
            TIntArrayList minePos = new TIntArrayList();
            List<IntDomainVar> otherEnds = new ArrayList<>();
            List<IntDomainVar> mineEnds = new ArrayList<>();
            Mapping map = rp.getSourceModel().getMapping();
            for (UUID vm : map.getRunningVMs()) {
                if (!vms.contains(vm)) {
                    otherPos.add(rp.getNode(map.getVMLocation(vm)));
                    VMActionModel a = rp.getVMAction(vm);
                    otherEnds.add(a.getCSlice().getEnd());
                } else {
                    minePos.add(rp.getNode(map.getVMLocation(vm)));
                    VMActionModel a = rp.getVMAction(vm);
                    mineEnds.add(a.getCSlice().getEnd());
                }
            }
            for (UUID vm : vms) {
                VMActionModel a = rp.getVMAction(vm);
                Precedences prec = new Precedences(s.getEnvironment(), a.getDSlice().getHoster(),
                        a.getDSlice().getStart(),
                        otherPos.toNativeArray(),
                        otherEnds.toArray(new IntDomainVar[otherEnds.size()]));
                s.post(prec);
            }

            //TODO: The following reveals a model problem. Too many constraints!!
            for (UUID vm : otherVMs) {
                VMActionModel a = rp.getVMAction(vm);
                Precedences prec = new Precedences(s.getEnvironment(), a.getDSlice().getHoster(),
                        a.getDSlice().getStart(),
                        minePos.toNativeArray(),
                        mineEnds.toArray(new IntDomainVar[mineEnds.size()]));
                s.post(prec);
            }
        }
        return true;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        Set<UUID> bad = new HashSet<>();
        Set<UUID> hosters = new HashSet<>();
        Collection<UUID> vms = cstr.getInvolvedVMs();
        Mapping map = m.getMapping();
        for (UUID vm : vms) {
            if (map.getRunningVMs().contains(vm)) {
                hosters.add(map.getVMLocation(vm));
            }
        }
        for (UUID n : hosters) { //Every used node that host a VMs that is not a part of the constraint
            //is a bad node. All the hosted VMs are candidate for relocation. Not optimal, but safe
            for (UUID vm : map.getRunningVMs(n)) {
                if (!vms.contains(vm)) {
                    bad.addAll(map.getRunningVMs(n));
                    break;
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
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoSatConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return Lonely.class;
        }

        @Override
        public CLonely build(SatConstraint cstr) {
            return new CLonely((Lonely) cstr);
        }
    }
}
