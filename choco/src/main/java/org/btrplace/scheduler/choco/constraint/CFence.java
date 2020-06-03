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
import org.btrplace.model.constraint.Fence;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;


/**
 * Choco implementation of {@link org.btrplace.model.constraint.Fence}.
 *
 * @author Fabien Hermenier
 */
public class CFence implements ChocoConstraint {

  private final Fence cstr;

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CFence(Fence c) {
        cstr = c;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) {

        if (cstr.isContinuous()) {
            for (VM vm : cstr.getInvolvedVMs()) {
                Node location = rp.getSourceModel().getMapping().getVMLocation(vm);
                if (location != null && !cstr.getInvolvedNodes().contains(location)) {
                    rp.getLogger().error("Constraint {} is not satisfied initially", cstr);
                    return false;
                }
            }
        }

        VM vm = cstr.getInvolvedVMs().iterator().next();
        Collection<Node> nodes = cstr.getInvolvedNodes();
        Slice t = rp.getVMAction(vm).getDSlice();
        if (!rp.getFutureRunningVMs().contains(vm)) {
            return true;
        }
        if (nodes.size() == 1) {
            return force(rp, t.getHoster(), vm, nodes.iterator().next());
        }
        return allBut(rp, t.getHoster(), vm, nodes);
    }

    private static boolean allBut(ReconfigurationProblem rp, IntVar hoster, VM vm, Collection<Node> nodes) {
        for (Node n : rp.getNodes()) {
            int idx = rp.getNode(n);
            if (!nodes.contains(n)) {
                try {
                    hoster.removeValue(idx, Cause.Null);
                } catch (ContradictionException ex) {
                    rp.getLogger().error("Unable to prevent VM '" + vm + "' to run on node '" + n + "'", ex);
                    return false;
                }
            }
        }
        return true;
    }


    private static boolean force(ReconfigurationProblem rp, IntVar h, VM vm, Node n) {
        //Only 1 possible destination node, so we directly instantiate the variable.
        try {
            h.instantiateTo(rp.getNode(n), Cause.Null);
        } catch (ContradictionException ex) {
            rp.getLogger().error("Unable to force VM '" + vm + "' to be running on node '" + n + "'", ex);
            return false;
        }
        return true;
    }


    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        Mapping map = i.getModel().getMapping();
        VM vm = cstr.getInvolvedVMs().iterator().next();
        if (map.isRunning(vm) && !cstr.getInvolvedNodes().contains(map.getVMLocation(vm))) {
            return Collections.singleton(vm);
        }
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return cstr.toString();
    }
}
