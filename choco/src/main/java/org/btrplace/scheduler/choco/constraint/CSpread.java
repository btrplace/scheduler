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
import org.btrplace.model.constraint.Spread;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.extensions.ChocoUtils;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.*;

/**
 * Continuous implementation of {@link Spread}.
 *
 * @author Fabien Hermenier
 */
public class CSpread implements ChocoConstraint {

  private final Spread cstr;

    /**
     * Make a new constraint.
     *
     * @param s the constraint to rely one
     */
    public CSpread(Spread s) {
        cstr = s;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) {
      if (cstr.isContinuous()) {
        Set<Node> usedNodes = new HashSet<>();
        for (VM vm : cstr.getInvolvedVMs()) {
          Node node = rp.getSourceModel().getMapping().getVMLocation(vm);
          if (node != null && !usedNodes.add(node)) {
              rp.getLogger().debug("Constraint {} is not satisfied initially", cstr);
            //System.out.println(rp.getSourceModel().getMapping());
            return false;
          }
        }
      }
        List<IntVar> running = placementVariables(rp);

        Model csp = rp.getModel();
        if (running.isEmpty()) {
            return true;
        }

        //The lazy spread implementation for the placement
      csp.post(csp.allDifferent(running.toArray(new IntVar[running.size()]), "AC"));
        if (cstr.isContinuous()) {
            List<VM> vms = new ArrayList<>(cstr.getInvolvedVMs());
            for (int i = 0; i < vms.size(); i++) {
                VM vm = vms.get(i);
                VMTransition aI = rp.getVMAction(vm);
                for (int j = 0; j < i; j++) {
                    VM vmJ = vms.get(j);
                    VMTransition aJ = rp.getVMAction(vmJ);
                    disallowOverlap(rp, aI, aJ);
                }
            }
        }
        return true;
    }

    private static void disallowOverlap(ReconfigurationProblem rp, VMTransition t1, VMTransition t2) {

      Slice dI = t1.getDSlice();
        Slice cI = t1.getCSlice();

        Slice dJ = t2.getDSlice();
        Slice cJ = t2.getCSlice();


        if (dI != null && cJ != null) {
            precedenceIfOverlap(rp, dI, cJ);
        }
        //The inverse relation
        if (dJ != null && cI != null) {
            precedenceIfOverlap(rp, dJ, cI);
        }
    }

    /**
     * Establish the precedence constraint {@code c.getEnd() <= d.getStart()} if the two slices may overlap.
     */
    private static void precedenceIfOverlap(ReconfigurationProblem rp, Slice d, Slice c) {
        Model csp = rp.getModel();
        //No need to place the constraints if the slices do not have a chance to overlap
        if (!(c.getHoster().isInstantiated() && !d.getHoster().contains(c.getHoster().getValue()))
                && !(d.getHoster().isInstantiated() && !c.getHoster().contains(d.getHoster().getValue()))
                ) {
            BoolVar eq;
            if (rp.labelVariables()) {
                eq = csp.boolVar(rp.makeVarLabel(d.getHoster(), "", c.getHoster(), "?"));
            } else {
                eq = csp.boolVar();
            }

          rp.getModel().arithm(d.getHoster(), "=", c.getHoster()).reifyWith(eq);
          org.chocosolver.solver.constraints.Constraint leqCstr = rp.getModel().arithm(c.getEnd(), "<=", d.getStart());
            ChocoUtils.postImplies(rp, eq, leqCstr);
        }
    }

    private List<IntVar> placementVariables(ReconfigurationProblem rp) {
        List<IntVar> running = new ArrayList<>();
        for (VM vmId : cstr.getInvolvedVMs()) {
            if (rp.getFutureRunningVMs().contains(vmId)) {
                VMTransition a = rp.getVMAction(vmId);
                Slice d = a.getDSlice();
                if (d != null) {
                    running.add(d.getHoster());
                }
            }
        }
        return running;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        Map<Node, Set<VM>> spots = new HashMap<>();
        Set<VM> bad = new HashSet<>();
        Mapping map = i.getModel().getMapping();
        for (VM vm : cstr.getInvolvedVMs()) {
            Node h = map.getVMLocation(vm);
            if (map.isRunning(vm)) {
                if (!spots.containsKey(h)) {
                    spots.put(h, new HashSet<>());
                }
                spots.get(h).add(vm);
            }

        }
        for (Map.Entry<Node, Set<VM>> e : spots.entrySet()) {
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
}
