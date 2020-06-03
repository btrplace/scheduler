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
import org.btrplace.model.constraint.Ban;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;


/**
 * Choco implementation of the constraint {@link Ban}.
 *
 * @author Fabien Hermenier
 */
public class CBan implements ChocoConstraint {

  private final Ban ban;

    /**
     * Make a new constraint.
     *
     * @param b the ban constraint to rely on
     */
    public CBan(Ban b) {
        ban = b;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) {

        if (ban.isContinuous()) {
            for (VM vm : ban.getInvolvedVMs()) {
                if (ban.getInvolvedNodes().contains(rp.getSourceModel().getMapping().getVMLocation(vm))) {
                    rp.getLogger().error("Constraint {} is not satisfied initially", ban);
                    return false;
                }
            }
        }
        Collection<Node> nodes = ban.getInvolvedNodes();
        int[] nodesIdx = new int[nodes.size()];
        int i = 0;
        for (Node n : ban.getInvolvedNodes()) {
            nodesIdx[i++] = rp.getNode(n);
        }

        VM vm = ban.getInvolvedVMs().iterator().next();
        Slice t = rp.getVMAction(vm).getDSlice();
        if (t != null) {
            for (int x : nodesIdx) {
                try {
                    t.getHoster().removeValue(x, Cause.Null);
                } catch (ContradictionException e) {
                    rp.getLogger().error("Unable to disallow " + vm + " to be running on " + rp.getNode(x), e);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        Mapping map = i.getModel().getMapping();
        VM vm = ban.getInvolvedVMs().iterator().next();
        if (map.isRunning(vm) && ban.getInvolvedNodes().contains(map.getVMLocation(vm))) {
            return Collections.singleton(vm);
        }
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return ban.toString();
    }
}
