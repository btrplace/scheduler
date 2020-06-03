/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
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
import org.btrplace.scheduler.choco.view.AliasedCumulatives;
import org.btrplace.scheduler.choco.view.ChocoView;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Arrays;
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
        if (cstr.isContinuous() && !injectContinuous(rp)) {
            return false;
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

    private boolean injectContinuous(ReconfigurationProblem rp) throws SchedulerException {
        Model csp = rp.getModel();
        //The constraint must be already satisfied
        if (!cstr.isSatisfied(rp.getSourceModel())) {
            rp.getLogger().error("The constraint '{}' must be already satisfied to provide a continuous restriction", cstr);
            return false;
        }
        int[] alias = new int[cstr.getInvolvedNodes().size()];
        int i = 0;
        for (Node n : cstr.getInvolvedNodes()) {
            alias[i++] = rp.getNode(n);
        }
        int nbRunning = 0;
        for (Node n : rp.getSourceModel().getMapping().getOnlineNodes()) {
            nbRunning += rp.getSourceModel().getMapping().getRunningVMs(n).size();
        }
        int[] cUse = new int[nbRunning];
        IntVar[] dUse = new IntVar[rp.getFutureRunningVMs().size()];
        Arrays.fill(cUse, 1);
        Arrays.fill(dUse, csp.intVar(1));

      ChocoView v = rp.getRequiredView(AliasedCumulatives.VIEW_ID);
        ((AliasedCumulatives) v).addDim(cstr.getAmount(), cUse, dUse, alias);
        return true;
    }

    private boolean filterWithSingleNode(ReconfigurationProblem rp) {
        Node n = cstr.getInvolvedNodes().iterator().next();
        IntVar v = rp.getNbRunningVMs().get(rp.getNode(n));
        Model csp = rp.getModel();
        csp.post(csp.arithm(v, "<=", cstr.getAmount()));


        return !cstr.isContinuous() || injectContinuous(rp);
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
