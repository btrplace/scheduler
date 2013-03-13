package btrplace.plan;

import btrplace.plan.event.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class DependenciesExtractor implements ActionVisitor {

    private Map<Action, Set<Action>> deps;

    public DependenciesExtractor() {
        deps = new HashMap<Action, Set<Action>>();
    }

    @Override
    public Object visit(Allocate a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(AllocateEvent a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(BootNode a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(BootVM a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(ForgeVM a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(KillVM a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(MigrateVM a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(ResumeVM a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(ShutdownNode a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(ShutdownVM a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(SuspendVM a) {
        throw new UnsupportedOperationException();
    }

    public Set<Action> getDependencies(Action a) {
        return deps.get(a);
    }
}
