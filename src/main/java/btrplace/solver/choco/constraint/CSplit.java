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
import btrplace.model.constraint.Split;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.chocoUtil.Disjoint;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.*;

/**
 * Choco implementation of the {@link btrplace.model.constraint.Split} constraint.
 * <p/>
 * TODO: continuous implementation
 *
 * @author Fabien Hermenier
 */
public class CSplit implements ChocoSatConstraint {

    private Split cstr;

    /**
     * Make a new constraint.
     *
     * @param s the constraint to rely on
     */
    public CSplit(Split s) {
        this.cstr = s;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        List<List<IntDomainVar>> groups = new ArrayList<List<IntDomainVar>>();
        for (Set<UUID> grp : cstr.getSets()) {
            List<IntDomainVar> l = new ArrayList<IntDomainVar>();
            for (UUID vm : grp) {
                if (rp.getFutureRunningVMs().contains(vm)) {
                    Slice s = rp.getVMAction(vm).getDSlice();
                    l.add(s.getHoster());
                }
            }
            if (!l.isEmpty()) {
                groups.add(l);
            }
        }
        CPSolver s = rp.getSolver();
        int nbNodes = rp.getNodes().length;
        IntDomainVar[][] vars = new IntDomainVar[groups.size()][];
        for (int i = 0; i < groups.size(); i++) {
            for (int j = 0; j < i; j++) {
                IntDomainVar[] gI = vars[i];
                IntDomainVar[] gJ = vars[j];

                if (gI == null) {
                    gI = groups.get(i).toArray(new IntDomainVar[groups.get(i).size()]);
                    vars[i] = gI;
                }

                if (gJ == null) {
                    gJ = groups.get(j).toArray(new IntDomainVar[groups.get(j).size()]);
                    vars[j] = gJ;
                }

                s.post(new Disjoint(s.getEnvironment(), gI, gJ, nbNodes));
            }
        }
        if (cstr.isContinuous()) {
            rp.getLogger().error("Continuous restriction is not supported for constraint split");
            return false;
        }
        return true;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        Mapping map = m.getMapping();
        List<Set<UUID>> groups = new ArrayList<Set<UUID>>(cstr.getSets());
        //Bad contains the VMs on nodes that host VMs from different groups.
        Set<UUID> bad = new HashSet<UUID>();
        for (Set<UUID> grp : groups) {
            for (UUID vm : grp) {
                if (map.getRunningVMs().contains(vm)) {
                    UUID n = map.getVMLocation(vm);
                    Set<UUID> allOnN = map.getRunningVMs(n);
                    for (UUID vmOnN : allOnN) {
                        if (inOtherGroup(groups, grp, vmOnN)) { //The VM belong to another group
                            bad.add(vm);
                            bad.add(vmOnN);
                        }
                    }
                }
            }
        }
        return bad;
    }

    private boolean inOtherGroup(List<Set<UUID>> groups, Set<UUID> grp, UUID vmOnN) {
        for (Set<UUID> s : groups) {
            if (s.contains(vmOnN) && !grp.contains(vmOnN)) {
                return true;
            }
        }
        return false;
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
            return Split.class;
        }

        @Override
        public CSplit build(SatConstraint cstr) {
            return new CSplit((Split) cstr);
        }
    }
}
