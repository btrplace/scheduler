package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.constraint.MaxOnline;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.ShutdownNode;

import java.util.HashSet;
import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/16/13
 * Time: 2:46 PM
 */
public class MaxOnlinesChecker extends AllowAllConstraintChecker<MaxOnline> {
    /**
     * Make a new checker.
     *
     * @param cstr the constraint associated to the checker.
     */

    private int current_online;

    public MaxOnlinesChecker(MaxOnline cstr) {
        super(cstr);
    }

    @Override
    public boolean startsWith(Model mo) {
        Mapping map = mo.getMapping();
        Set<Node> onlineNodes = map.getOnlineNodes();
        // Keep the below line to not modify the RP variable
        Set<Node> onlineNodesCopy = new HashSet<Node>(onlineNodes);
        onlineNodesCopy.retainAll(getNodes());
        current_online = onlineNodesCopy.size();
        return true;
    }

    @Override
    public boolean start(BootNode a) {
        if (getConstraint().isContinuous() && getNodes().contains(a.getNode()))
            return (current_online < getConstraint().getAmount());

        return true;
    }

    @Override
    public void end(BootNode a) {
        if (getNodes().contains(a.getNode())) current_online++;
    }

    @Override
    public void end(ShutdownNode a) {
        current_online--;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping map = mo.getMapping();
        Set<Node> onlineNodes = map.getOnlineNodes();
        // Keep the below line to not modify the RP variable
        Set<Node> onlineNodesCopy = new HashSet<Node>(onlineNodes);
        onlineNodesCopy.retainAll(getNodes());
        current_online = onlineNodesCopy.size();
        return (current_online <= getConstraint().getAmount());
    }
}
