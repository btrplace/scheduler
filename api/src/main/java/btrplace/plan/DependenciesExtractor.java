package btrplace.plan;

import btrplace.model.Model;
import btrplace.model.view.ShareableResource;
import btrplace.plan.event.*;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class DependenciesExtractor implements ActionVisitor {

    private Map<Action, Set<Action>> deps;

    private Map<UUID, Set<Action>> freeings;

    private Map<UUID, Set<Action>> demandings;

    private Model origin;

    /**
     * Make a new instance.
     *
     * @param o the model at the source of the reconfiguration plan
     */
    public DependenciesExtractor(Model o) {
        deps = new HashMap<Action, Set<Action>>();
        demandings = new HashMap<UUID, Set<Action>>();
        freeings = new HashMap<UUID, Set<Action>>();
        origin = o;
    }

    private Set<Action> getFreeings(UUID u) {
        Set<Action> actions = freeings.get(u);
        if (actions == null) {
            actions = new HashSet<Action>();
            freeings.put(u, actions);
        }
        return actions;
    }

    private Set<Action> getDemandings(UUID u) {
        Set<Action> actions = demandings.get(u);
        if (actions == null) {
            actions = new HashSet<Action>();
            demandings.put(u, actions);
        }
        return actions;
    }

    @Override
    public Boolean visit(Allocate a) {
        //If the resource allocation is increasing, it's
        //a consuming action. Otherwise, it's a freeing action
        String rcId = a.getResourceId();
        int newAmount = a.getAmount();
        ShareableResource rc = (ShareableResource) origin.getView(ShareableResource.VIEW_ID_BASE + rcId);
        if (rc == null) {
            return false;
        }
        int oldAmount = rc.get(a.getVM());
        if (newAmount > oldAmount) {
            getDemandings(a.getHost()).add(a);
        } else {
            getFreeings(a.getHost()).add(a);
        }
        return true;
    }

    @Override
    public Boolean visit(AllocateEvent a) {
        return true;
    }

    @Override
    public Boolean visit(BootNode a) {
        return getFreeings(a.getNode()).add(a);
    }

    @Override
    public Boolean visit(BootVM a) {
        return getDemandings(a.getDestinationNode()).add(a);
    }

    @Override
    public Boolean visit(ForgeVM a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean visit(KillVM a) {
        return getFreeings(a.getNode()).add(a);
    }

    @Override
    public Boolean visit(MigrateVM a) {
        return getFreeings(a.getSourceNode()).add(a) && getDemandings(a.getDestinationNode()).add(a);
    }

    @Override
    public Boolean visit(ResumeVM a) {
        return getDemandings(a.getDestinationNode()).add(a);
    }

    @Override
    public Boolean visit(ShutdownNode a) {
        return getDemandings(a.getNode()).add(a);
    }

    @Override
    public Boolean visit(ShutdownVM a) {
        return getFreeings(a.getNode()).add(a);
    }

    @Override
    public Boolean visit(SuspendVM a) {
        return getFreeings(a.getSourceNode()).add(a);
    }

    public Set<Action> getDependencies(Action a) {
        //Get all the freeing actions associated to this one, return those who end before this action.
        return deps.get(a);
    }
}
