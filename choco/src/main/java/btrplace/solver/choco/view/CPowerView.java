package btrplace.solver.choco.view;

import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.actionModel.BootableNodeModel;
import btrplace.solver.choco.actionModel.NodeActionModel;
import btrplace.solver.choco.actionModel.ShutdownableNodeModel;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Tu Huynh Dang
 * Date: 6/4/13
 * Time: 9:17 PM
 */
public class CPowerView implements ChocoModelView {

    public static final String VIEW_ID = "PowerTime";
    private Map<Integer, IntDomainVar> powerStarts;
    private Map<Integer, IntDomainVar> powerEnds;

    public CPowerView(ReconfigurationProblem rp) {
        CPSolver solver = rp.getSolver();
        powerStarts = new HashMap<Integer, IntDomainVar>(rp.getNodes().length);
        powerEnds = new HashMap<Integer, IntDomainVar>(rp.getNodes().length);

        for (Node n : rp.getNodes()) {
            NodeActionModel na = rp.getNodeAction(n);
            if (na instanceof ShutdownableNodeModel) {
                powerStarts.put(rp.getNode(n), rp.getStart());
                IntDomainVar powerEnd = rp.makeUnboundedDuration("NodeAction(", n, ").Pe");
                solver.post(solver.eq(powerEnd, solver.plus(na.getHostingEnd(), na.getDuration())));
                powerEnds.put(rp.getNode(n), powerEnd);
            }
            else if (na instanceof BootableNodeModel) {
                powerStarts.put(rp.getNode(n), na.getStart());
                powerEnds.put(rp.getNode(n), na.getHostingEnd());
            }
        }
    }

    public IntDomainVar getPowerStart(int idx) {
        return powerStarts.get(idx);
    }

    public IntDomainVar getPowerEnd(int idx) {
        return powerEnds.get(idx);
    }

    @Override
    public String getIdentifier() {
        return VIEW_ID;
    }

    @Override
    public boolean beforeSolve(ReconfigurationProblem rp) {
        return true;
    }

    @Override
    public boolean insertActions(ReconfigurationProblem rp, ReconfigurationPlan p) {
        return true;
    }

    @Override
    public boolean cloneVM(VM vm, VM clone) {
        return true;
    }
}