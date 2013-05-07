package btrplace.solver.choco.objective.minMTTR;

import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.actionModel.NodeActionModel;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Very basic variable selector that focus the moment where node actions consume.
 *
 * @author Fabien Hermenier
 */
public class StartingNodes extends AbstractIntVarSelector {

    private NodeActionModel[] actions;

    private String lbl;

    private ReconfigurationProblem rp;

    /**
     * Make a new heuristic.
     *
     * @param lbl         the heuristic label (for debugging purpose)
     * @param rp          the problem to consider
     * @param nodeActions the actions to consider
     */
    public StartingNodes(String lbl, ReconfigurationProblem rp, NodeActionModel[] nodeActions) {
        super(rp.getSolver());
        actions = nodeActions;
        this.lbl = lbl;
        this.rp = rp;
    }

    @Override
    public IntDomainVar selectVar() {
        for (NodeActionModel na : actions) {
            if (!na.getStart().isInstantiated()) {
                return na.getStart();
            }
        }
        rp.getLogger().debug("{} - no more nodes to handle", lbl);
        return null;
    }
}
