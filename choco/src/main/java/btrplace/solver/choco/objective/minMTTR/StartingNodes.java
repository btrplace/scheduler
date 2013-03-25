package btrplace.solver.choco.objective.minMTTR;

import btrplace.solver.choco.NodeActionModel;
import btrplace.solver.choco.ReconfigurationProblem;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * @author Fabien Hermenier
 */
public class StartingNodes extends AbstractIntVarSelector {

    private NodeActionModel[] actions;

    private String lbl;

    private ReconfigurationProblem rp;

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
