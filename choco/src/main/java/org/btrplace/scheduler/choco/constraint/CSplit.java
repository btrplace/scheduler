/*
 * Copyright  2022 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import gnu.trove.list.array.TIntArrayList;
import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Split;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.extensions.Disjoint;
import org.btrplace.scheduler.choco.extensions.Precedences;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import java.util.*;

/**
 * Choco implementation of the {@link org.btrplace.model.constraint.Split} constraint.
 *
 * @author Fabien Hermenier
 */
public class CSplit implements ChocoConstraint {

  private final Split cstr;

    /**
     * Make a new constraint.
     *
     * @param s the constraint to rely on
     */
    public CSplit(Split s) {
        this.cstr = s;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
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
        Model csp = rp.getModel();
        int nbNodes = rp.getNodes().size();
        IntVar[][] vars = new IntVar[groups.size()][];
        for (int i = 0; i < groups.size(); i++) {
            vars[i] = groups.get(i).toArray(new IntVar[groups.get(i).size()]);
        }
        csp.post(new Disjoint(vars, nbNodes));

        return !(cstr.isContinuous() && !injectContinuous(rp, vmGroups));
    }

    private boolean injectContinuous(ReconfigurationProblem rp, List<List<VM>> vmGroups) {
        if (!cstr.isSatisfied(rp.getSourceModel())) {
            rp.getLogger().debug("The constraint '{}' must be already satisfied to provide a continuous restriction", cstr);
            return false;
        }
        //Each VM on a group, can not go to a node until all the VMs from the other groups have leaved
        //So, for each group of VM, we create a list containing the c^end and the c^host variable of all
        //the VMs in the other groups then we establish precedences constraints.

        TIntArrayList[] otherPositions = new TIntArrayList[vmGroups.size()];
        @SuppressWarnings("unchecked")
        List<IntVar>[] otherEnds = new List[vmGroups.size()];
        for (int i = 0; i < vmGroups.size(); i++) {
            otherPositions[i] = new TIntArrayList();
            otherEnds[i] = new ArrayList<>();
        }

        fullfillOthers(rp, otherPositions, otherEnds, vmGroups);

        //Now, we just have to put way too many precedences constraint, one per VM.
        for (int i = 0; i < vmGroups.size(); i++) {
            List<VM> grp = vmGroups.get(i);
            for (VM vm : grp) {
                if (rp.getFutureRunningVMs().contains(vm)) {
                    VMTransition a = rp.getVMAction(vm);
                    IntVar myPos = a.getDSlice().getHoster();
                    IntVar myStart = a.getDSlice().getStart();
                    rp.getModel().post(new Precedences(myPos,
                            myStart,
                            otherPositions[i].toArray(),
                            otherEnds[i].toArray(new IntVar[otherEnds[i].size()])));
                }
            }
        }
        return true;
    }

    private static void fullfillOthers(ReconfigurationProblem rp, TIntArrayList[] otherPositions, List<IntVar>[] otherEnds, List<List<VM>> vmGroups) {
        Mapping map = rp.getSourceModel().getMapping();
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
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        Mapping map = i.getModel().getMapping();
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

    private static boolean inOtherGroup(List<Collection<VM>> groups, Collection<VM> grp, VM vmOnN) {
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
}
