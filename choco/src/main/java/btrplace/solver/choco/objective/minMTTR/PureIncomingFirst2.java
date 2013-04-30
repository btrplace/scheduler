/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

package btrplace.solver.choco.objective.minMTTR;

import btrplace.model.Mapping;
import btrplace.solver.choco.*;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

/**
 * An heuristic to branch first on the consume moment of actions
 * that arrive on nodes without any outgoing actions.
 *
 * @author Fabien Hermenier
 */
public class PureIncomingFirst2 extends AbstractIntVarSelector {

    private IntDomainVar[] hoster;

    private IntDomainVar[] starts;

    private List<UUID> vms;

    private int[] oldPos;

    private BitSet[] outs;

    private BitSet[] ins;

    private boolean first = true;

    private ReconfigurationProblem pb;

    /**
     * Make a new heuristics
     *
     * @param rp      the problem to rely on
     * @param actions the actions to consider.
     */
    public PureIncomingFirst2(ReconfigurationProblem rp, List<ActionModel> actions) {
        super(rp.getSolver(), ActionModelUtils.getStarts(actions.toArray(new ActionModel[actions.size()])));
        this.pb = rp;
        Mapping cfg = rp.getSourceModel().getMapping();

        VMActionModel[] vmActions = rp.getVMActions();

        hoster = new IntDomainVar[vmActions.length];
        starts = new IntDomainVar[vmActions.length];

        this.vms = new ArrayList<UUID>(rp.getFutureRunningVMs());

        oldPos = new int[hoster.length];
        outs = new BitSet[rp.getNodes().length];
        ins = new BitSet[rp.getNodes().length];
        for (int i = 0; i < rp.getNodes().length; i++) {
            outs[i] = new BitSet();
            ins[i] = new BitSet();
        }

        for (int i = 0; i < hoster.length; i++) {
            VMActionModel action = vmActions[i];
            Slice slice = action.getDSlice();
            if (slice != null) {
                IntDomainVar h = slice.getHoster();
                IntDomainVar s = slice.getStart();
                hoster[i] = h;
                if (s != rp.getEnd()) {
                    starts[i] = s;
                }
                UUID vm = action.getVM();
                UUID n = cfg.getVMLocation(vm);
                if (n == null) {
                    oldPos[i] = -1;
                } else {
                    oldPos[i] = rp.getNode(n);
                    outs[rp.getNode(n)].set(i);     //VM i was on node n
                }
            }
        }
    }

    @Override
    public IntDomainVar selectVar() {

        for (int i = 0; i < ins.length; i++) {
            ins[i].clear();
        }

        BitSet stays = new BitSet();
        //At this moment, all the hoster of the demanding slices are computed.
        //for each node, we compute the number of incoming and outgoing
        for (int i = 0; i < hoster.length; i++) {
            if (hoster[i] != null && hoster[i].isInstantiated()) {
                int newPos = hoster[i].getVal();
                if (oldPos[i] != -1 && newPos != oldPos[i]) {
                    //The VM has move
                    ins[newPos].set(i);
                } else if (oldPos[i] != -1 && newPos == oldPos[i]) {
                    stays.set(i);
                }
            }
        }

        //TODO: consume with nodes with a sufficient amount of free resources at startup
        for (int x = 0; x < outs.length; x++) {   //Node per node
            if (outs[x].cardinality() == 0) { //no outgoing VMs, can be launched directly.
                BitSet in = ins[x];
                for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
                    if (starts[i] != null && !starts[i].isInstantiated()) {
                        return starts[i];
                    }
                }
            }
        }
        //TODO: Decreasing stay at end
        //TODO: association between slice on the same node
        for (int i = stays.nextSetBit(0); i >= 0; i = stays.nextSetBit(i + 1)) {
            if (starts[i] != null && !starts[i].isInstantiated()) {
                return starts[i];
            }
        }

        return minInf();
    }

    private IntDomainVar minInf() {
        IntDomainVar best = null;
        for (int i = 0; i < starts.length; i++) {
            IntDomainVar v = starts[i];
            if (i < vms.size() - 1) {
                UUID vm = vms.get(i);
                if (vm != null && v != null && !v.isInstantiated() &&
                        (best == null || best.getInf() > v.getInf())) {
                    best = v;
                }
            }
        }
        return best;
    }
}