package btrplace.solver.choco.objective.minMTTR;

import btrplace.model.Mapping;
import btrplace.solver.choco.ActionModelUtils;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.VMActionModel;
import btrplace.solver.choco.actionModel.ActionModel;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

/**
 * A heuristic that first focus on scheduling the VMs
 * on nodes that are the source of actions liberating resources.
 * <p/>
 * For performance reason, the VM placement is put into a cache
 * that must be invalidated each time the placement is modified
 *
 * @author Fabien Hermenier
 */
public class OnStableNodeFirst extends AbstractIntVarSelector {

    private IntDomainVar[] hoster;

    private IntDomainVar[] starts;

    private List<UUID> vms;

    private int[] oldPos;

    private BitSet[] outs;

    private BitSet[] ins;


    private MinMTTR obj;

    private IStateInt firstFree;

    /**
     * Make a new heuristics
     *
     * @param lbl     the heuristic label (for debugging purpose)
     * @param rp      the problem to rely on
     * @param actions the actions to consider.
     */
    public OnStableNodeFirst(String lbl, ReconfigurationProblem rp, List<ActionModel> actions, MinMTTR obj) {
        super(rp.getSolver(), ActionModelUtils.getStarts(actions.toArray(new ActionModel[actions.size()])));
        firstFree = rp.getSolver().getEnvironment().makeInt(0);
        this.obj = obj;
        Mapping cfg = rp.getSourceModel().getMapping();

        VMActionModel[] vmActions = rp.getVMActions();

        hoster = new IntDomainVar[vmActions.length];
        starts = new IntDomainVar[vmActions.length];

        this.vms = new ArrayList<>(rp.getFutureRunningVMs());

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
                    //VM i was on node n
                    outs[rp.getNode(n)].set(i);
                }
            }
        }
    }

    private BitSet stays, move;

    /**
     * Invalidate the VM placement.
     * This must be called each time the placement is modified to
     * clear the VM placement cache.
     */
    public void invalidPlacement() {
        stays = null;
        move = null;
    }

    @Override
    public IntDomainVar selectVar() {

        for (BitSet in : ins) {
            in.clear();
        }

        //At this moment, all the hosters of the demanding slices are computed.
        //for each node, we compute the number of incoming and outgoing
        if (stays == null && move == null) {
            stays = new BitSet();
            move = new BitSet();
            for (int i = 0; i < hoster.length; i++) {
                if (hoster[i] != null && hoster[i].isInstantiated()) {
                    int newPos = hoster[i].getVal();
                    if (oldPos[i] != -1 && newPos != oldPos[i]) {
                        //The VM has move
                        ins[newPos].set(i);
                        move.set(i);
                    } else if (newPos == oldPos[i]) {
                        stays.set(i);
                    }
                }
            }
        }


        //VMs going on nodes with no outgoing actions, so actions that can consume with no delay
        for (int x = 0; x < outs.length; x++) {
            if (outs[x].cardinality() == 0) {
                //no outgoing VMs, can be launched directly.
                BitSet in = ins[x];
                for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
                    if (starts[i] != null && !starts[i].isInstantiated()) {
                        return starts[i];
                    }
                }
            }
        }

        //VMs that are moving
        for (int i = move.nextSetBit(0); i >= 0; i = move.nextSetBit(i + 1)) {
            if (starts[i] != null && !starts[i].isInstantiated()) {
                if (oldPos[i] != hoster[i].getVal()) {
                    return starts[i];
                }
            }
        }

        IntDomainVar earlyVar = null;
        for (int i = stays.nextSetBit(0); i >= 0; i = stays.nextSetBit(i + 1)) {
            if (starts[i] != null && !starts[i].isInstantiated()) {
                if (earlyVar == null) {
                    earlyVar = starts[i];
                } else {
                    if (earlyVar.getInf() > starts[i].getInf()) {
                        earlyVar = starts[i];
                    }
                }
            }
        }
        if (earlyVar != null) {
            return earlyVar;
        }

        return minInf();
    }

    private IntDomainVar minInf() {
        IntDomainVar best = null;
        for (int i = firstFree.get(); i < starts.length; i++) {
            IntDomainVar v = starts[i];
            if (i < vms.size() - 1) {
                UUID vm = vms.get(i);
                if (vm != null && v != null) {
                    if (!v.isInstantiated()) {
                        if (best == null || best.getInf() < v.getInf()) {
                            best = v;
                            if (best.getInf() == 0) {
                                break;
                            }
                        }
                    } else {
                        firstFree.increment();
                    }
                }
            }
        }
        if (best == null) {
            //Plug the cost constraints
            obj.postCostConstraints();
        }
        return best;
    }
}
