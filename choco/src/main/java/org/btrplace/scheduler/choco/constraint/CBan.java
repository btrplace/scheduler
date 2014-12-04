/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Ban;
import org.btrplace.model.constraint.Constraint;
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

    private Ban ban;

    /**
     * Make a new constraint.
     *
     * @param b the ban constraint to rely on
     */
    public CBan(Ban b) {
        ban = b;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) {

        if (ban.isContinuous() && !ban.getChecker().startsWith(rp.getSourceModel())) {
            rp.getLogger().error("Constraint {} is not satisfied initially", ban);
            return false;
        }
        Collection<Node> nodes = ban.getInvolvedNodes();
        int[] nodesIdx = new int[nodes.size()];
        int i = 0;
        for (Node n : ban.getInvolvedNodes()) {
            nodesIdx[i++] = rp.getNode(n);
        }

        VM vm = ban.getInvolvedVMs().iterator().next();
        if (rp.getFutureRunningVMs().contains(vm)) {
            Slice t = rp.getVMAction(vm).getDSlice();
            if (t != null) {
                for (int x : nodesIdx) {
                    try {
                        t.getHoster().removeValue(x, Cause.Null);
                    } catch (ContradictionException e) {
                        rp.getLogger().error("Unable to disallow VM '{}' to be running on '{}': {}", vm, rp.getNode(x), e.getMessage());
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        Mapping map = m.getMapping();
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

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return Ban.class;
        }

        @Override
        public CBan build(Constraint cstr) {
            return new CBan((Ban) cstr);
        }
    }
}
