/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.RunningCapacity;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Choco implementation of {@link org.btrplace.model.constraint.RunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CRunningCapacity implements ChocoConstraint {

  private final RunningCapacity cstr;

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CRunningCapacity(RunningCapacity c) {
        cstr = c;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
        Model csp = rp.getModel();
        if (cstr.getInvolvedNodes().size() == 1) {
            return filterWithSingleNode(rp);
        }

        List<IntVar> vs = new ArrayList<>();
        for (Node u : cstr.getInvolvedNodes()) {
            vs.add(rp.getNbRunningVMs().get(rp.getNode(u)));
        }
        //Try to get a lower bound
        //basically, we count 1 per VM necessarily in the set of nodes
        //if involved nodes == all the nodes, then sum == nb of running VMs
        IntVar mySum = csp.intVar(rp.makeVarLabel("nbRunning"), 0, rp.getFutureRunningVMs().size(), true);
        csp.post(csp.sum(vs.toArray(new IntVar[vs.size()]), "=", mySum));
        csp.post(csp.arithm(mySum, "<=", cstr.getAmount()));

        if (cstr.getInvolvedNodes().equals(rp.getSourceModel().getMapping().getAllNodes())) {
            csp.post(csp.arithm(mySum, "=", rp.getFutureRunningVMs().size()));
        }
        return true;
    }

    private boolean filterWithSingleNode(ReconfigurationProblem rp) {
        Node n = cstr.getInvolvedNodes().iterator().next();
        IntVar v = rp.getNbRunningVMs().get(rp.getNode(n));
        Model csp = rp.getModel();
        csp.post(csp.arithm(v, "<=", cstr.getAmount()));
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        Mapping map = i.getModel().getMapping();
        Set<VM> bad = new HashSet<>();
        int remainder = cstr.getAmount();
        for (Node n : cstr.getInvolvedNodes()) {
            remainder -= map.getRunningVMs(n).size();
            if (remainder < 0) {
                for (Node n2 : cstr.getInvolvedNodes()) {
                    bad.addAll(map.getRunningVMs(n2));
                }
                return bad;
            }
        }
        return bad;
    }

    @Override
    public String toString() {
        return cstr.toString();
    }
}
