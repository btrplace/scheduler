/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import gnu.trove.list.array.TIntArrayList;
import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.ResourceCapacity;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.btrplace.scheduler.choco.view.AliasedCumulatives;
import org.btrplace.scheduler.choco.view.CShareableResource;
import org.btrplace.scheduler.choco.view.ChocoView;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Choco implementation of {@link org.btrplace.model.constraint.ResourceCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CResourceCapacity implements ChocoConstraint {

  private final ResourceCapacity cstr;

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CResourceCapacity(ResourceCapacity c) {
        cstr = c;
    }

    private boolean injectWithSingleNode(CShareableResource rcm, ReconfigurationProblem rp) {
        int amount = cstr.getAmount();
        Model csp = rp.getModel();
        Node n = cstr.getInvolvedNodes().iterator().next();
        int nIdx = rp.getNode(n);
        IntVar v = rcm.getVirtualUsage().get(nIdx);
        csp.post(csp.arithm(v, "<=", amount));

        // Notify the view about the future node capacity.
        rcm.minNodeCapacity(nIdx, amount);
        //Continuous in practice ?
        if (cstr.isContinuous()) {
            if (cstr.isSatisfied(rp.getSourceModel())) {
                try {
                    v.updateUpperBound(cstr.getAmount(), Cause.Null);
                } catch (ContradictionException e) {
                    rp.getLogger().error("Unable to restrict to up to " + cstr.getAmount() + ", the maximum '" + rcm.getResourceIdentifier() + "' usage on " + n, e);
                    return false;
                }
            } else {
                rp.getLogger().error("The constraint '{}' must be already satisfied to provide a continuous restriction", cstr);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {

        Model csp = rp.getModel();
        CShareableResource rcm = (CShareableResource) rp.getRequiredView(ShareableResource.getIdentifier(cstr.getResource()));

        if (cstr.getInvolvedNodes().size() == 1) {
            return injectWithSingleNode(rcm, rp);
        }

        if (cstr.isContinuous() && !injectContinuous(rp, rcm)) {
            return false;
        }

        List<IntVar> vs = new ArrayList<>();
        for (Node u : cstr.getInvolvedNodes()) {
            vs.add(rcm.getVirtualUsage().get(rp.getNode(u)));
        }

        IntVar mySum = csp.intVar(rp.makeVarLabel("usage(", rcm.getIdentifier(), ")"), 0, Integer.MAX_VALUE / 100, true);
        csp.post(csp.sum(vs.toArray(new IntVar[vs.size()]), "=", mySum));
        csp.post(csp.arithm(mySum, "<=", cstr.getAmount()));
        return true;
    }

    private boolean injectContinuous(ReconfigurationProblem rp, CShareableResource rcm) throws SchedulerException {
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

        TIntArrayList cUse = new TIntArrayList();
        List<IntVar> dUse = new ArrayList<>();

        for (VM vmId : rp.getVMs()) {
            VMTransition a = rp.getVMAction(vmId);
            Slice c = a.getCSlice();
            Slice d = a.getDSlice();
            if (c != null) {
                cUse.add(rcm.getSourceResource().getConsumption(vmId));
            }
            if (d != null) {
                int m = rcm.getFutureVMAllocation(rp.getVM(vmId));
                dUse.add(rp.fixed(m, "vmAllocation('", rcm.getResourceIdentifier(), "', '", vmId, "'"));
            }
        }
        ChocoView v = rp.getRequiredView(AliasedCumulatives.VIEW_ID);
        ((AliasedCumulatives) v).addDim(cstr.getAmount(), cUse.toArray(), dUse.toArray(new IntVar[dUse.size()]), alias);
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        if (cstr.getInvolvedNodes().size() <= 1) {
            //If there is only a single node, we delegate this work to CShareableResource.
            return Collections.emptySet();
        }
            Mapping map = i.getModel().getMapping();
            ShareableResource rc = ShareableResource.get(i.getModel(), cstr.getResource());
            if (rc == null) {
                return map.getRunningVMs(cstr.getInvolvedNodes());
            }
            Set<VM> bad = new HashSet<>();
            int remainder = cstr.getAmount();
            for (Node n : cstr.getInvolvedNodes()) {
                for (VM v : map.getRunningVMs(n)) {
                    remainder -= rc.getConsumption(v);
                    if (remainder < 0) {
                        for (Node n2 : cstr.getInvolvedNodes()) {
                            bad.addAll(map.getRunningVMs(n2));
                        }
                        return bad;
                    }
                }
            }
            return bad;
    }

    @Override
    public String toString() {
        return cstr.toString();
    }
}
