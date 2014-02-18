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
import btrplace.model.constraint.Split;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.actionModel.VMActionModel;
import btrplace.solver.choco.extensions.DisjointMultiple;
import btrplace.solver.choco.extensions.Precedences;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.variables.IntVar;

import java.util.*;

/**
 * Choco implementation of the {@link btrplace.model.constraint.Split} constraint.
 *
 * @author Fabien Hermenier
 */
public class CSplit implements ChocoConstraint {

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
        List<List<IntVar>> groups = new ArrayList<>();
        List<List<VM>> vmGroups = new ArrayList<>();
        for (Collection<VM> grp : cstr.getSets()) {
            List<IntVar> l = new ArrayList<>();
            List<VM> vl = new ArrayList<>();
            for (VM vm : grp) {
                if (rp.getFutureRunningVMs().contains(vm)) {
                    Slice s = rp.getVMAction(vm).getDSlice();
                    l.add(s.getHoster());
                    vl.add(vm);
                }
            }
            if (!l.isEmpty()) {
                groups.add(l);
                vmGroups.add(vl);
            }
        }
        Solver s = rp.getSolver();
        int nbNodes = rp.getNodes().length;
        IntVar[][] vars = new IntVar[groups.size()][];
        for (int i = 0; i < groups.size(); i++) {
            vars[i] = groups.get(i).toArray(new IntVar[groups.get(i).size()]);
        }
        s.post(new DisjointMultiple(s, vars, nbNodes));

        if (cstr.isContinuous()) {
            if (!cstr.isSatisfied(rp.getSourceModel())) {
                rp.getLogger().error("The constraint '{}' must be already satisfied to provide a continuous restriction", cstr);
                return false;
            } else {
                Mapping map = rp.getSourceModel().getMapping();
                //Each VM on a group, can not go to a node until all the VMs from the other groups have leaved
                //So, for each group of VM, we create a list containing the c^end and the c^host variable of all
                //the VMs in the other groups

                TIntArrayList[] otherPositions = new TIntArrayList[vmGroups.size()];
                List<IntVar>[] otherEnds = new List[vmGroups.size()];
                for (int i = 0; i < vmGroups.size(); i++) {
                    otherPositions[i] = new TIntArrayList();
                    otherEnds[i] = new ArrayList<>();
                }

                //Fulfill the others stuff.
                for (int i = 0; i < vmGroups.size(); i++) {
                    List<VM> grp = vmGroups.get(i);
                    for (VM vm : grp) {
                        if (map.isRunning(vm)) {
                            int myPos = rp.getNode(map.getVMLocation(vm));
                            IntVar myEnd = rp.getVMAction(vm).getCSlice().getEnd();

                            for (int j = 0; j < vmGroups.size(); j++) {
                                if (i != j) {
                                    otherPositions[j].add(myPos);
                                    otherEnds[j].add(myEnd);
                                }
                            }
                        }
                    }
                }
                int[][] otherPos = new int[groups.size()][];
                IntVar[][] otherEds = new IntVar[groups.size()][];
                for (int i = 0; i < vmGroups.size(); i++) {
                    otherPos[i] = otherPositions[i].toArray();
                    otherEds[i] = otherEnds[i].toArray(new IntVar[otherEnds[i].size()]);
                }

                //Now, we just have to put way too many precedences constraint, one per VM.
                for (int i = 0; i < vmGroups.size(); i++) {
                    List<VM> grp = vmGroups.get(i);
                    for (VM vm : grp) {
                        if (rp.getFutureRunningVMs().contains(vm)) {
                            VMActionModel a = rp.getVMAction(vm);
                            IntVar myPos = a.getDSlice().getHoster();
                            IntVar myStart = a.getDSlice().getStart();
                            s.post(new Precedences(s.getEnvironment(), myPos, myStart, otherPos[i], otherEds[i]));
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        Mapping map = m.getMapping();
        List<Collection<VM>> groups = new ArrayList<>(cstr.getSets());
        //Bad contains the VMs on nodes that host VMs from different groups.
        Set<VM> bad = new HashSet<>();
        for (Collection<VM> grp : groups) {
            for (VM vm : grp) {
                if (map.isRunning(vm)) {
                    Node n = map.getVMLocation(vm);
                    Set<VM> allOnN = map.getRunningVMs(n);
                    for (VM vmOnN : allOnN) {
                        if (inOtherGroup(groups, grp, vmOnN)) {
                            //The VM belong to another group
                            bad.add(vm);
                            bad.add(vmOnN);
                        }
                    }
                }
            }
        }
        return bad;
    }

    private boolean inOtherGroup(List<Collection<VM>> groups, Collection<VM> grp, VM vmOnN) {
        for (Collection<VM> s : groups) {
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
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return Split.class;
        }

        @Override
        public CSplit build(Constraint c) {
            return new CSplit((Split) c);
        }
    }
}
