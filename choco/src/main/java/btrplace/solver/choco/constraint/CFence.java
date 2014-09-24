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

package btrplace.solver.choco.constraint;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.Ban;
import btrplace.model.constraint.Constraint;
import btrplace.model.constraint.Fence;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import solver.Cause;
import solver.exception.ContradictionException;

import java.util.*;


/**
 * Choco implementation of {@link btrplace.model.constraint.Fence}.
 *
 * @author Fabien Hermenier
 */
public class CFence implements ChocoConstraint {

    private Fence cstr;

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CFence(Fence c) {
        cstr = c;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) {

        if (cstr.isContinuous() && !cstr.getChecker().startsWith(rp.getSourceModel())) {
            rp.getLogger().error("Constraint {} is not satisfied initially", cstr);
            return false;
        }

        VM vm = cstr.getInvolvedVMs().iterator().next();
        Collection<Node> nodes = cstr.getInvolvedNodes();
        if (rp.getFutureRunningVMs().contains(vm)) {
            if (nodes.size() == 1) {
                //Only 1 possible destination node, so we directly instantiate the variable.
                Slice t = rp.getVMAction(vm).getDSlice();
                Node n = nodes.iterator().next();
                try {
                    t.getHoster().instantiateTo(rp.getNode(n), Cause.Null);
                } catch (ContradictionException ex) {
                    rp.getLogger().error("Unable to force VM '{}' to be running on node '{}'", vm, n);
                    return false;
                }
            } else {
                //Transformation to a ban constraint that disallow all the other nodes
                List<Node> otherNodes = new ArrayList<>(rp.getNodes().length - nodes.size());
                for (Node n : rp.getNodes()) {
                    if (!nodes.contains(n)) {
                        otherNodes.add(n);
                    }
                }
                return new CBan(new Ban(vm, otherNodes)).inject(rp);

            }
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        Mapping map = m.getMapping();
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

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return Fence.class;
        }

        @Override
        public CFence build(Constraint c) {
            return new CFence((Fence) c);
        }
    }
}
