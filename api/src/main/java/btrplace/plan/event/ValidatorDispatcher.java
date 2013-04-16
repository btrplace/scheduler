package btrplace.plan.event;

import btrplace.plan.ReconfigurationPlanValidator;

import java.util.List;

/**
 * Dispatch acceptance requests to a list of {@link ReconfigurationPlanValidator}.
 *
 * @author Fabien Hermenier
 */
public class ValidatorDispatcher implements ActionVisitor {

    private List<ReconfigurationPlanValidator> listeners;

    /**
     * New dispatcher.
     *
     * @param l the list of validators
     */
    public ValidatorDispatcher(List<ReconfigurationPlanValidator> l) {
        listeners = l;
    }

    @Override
    public Boolean visit(SuspendVM a) {
        for (ReconfigurationPlanValidator l : listeners) {
            if (!l.accept(a)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visit(Allocate a) {
        for (ReconfigurationPlanValidator l : listeners) {
            if (!l.accept(a)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visit(AllocateEvent a) {
        for (ReconfigurationPlanValidator l : listeners) {
            if (!l.accept(a)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visit(SubstitutedVMEvent a) {
        for (ReconfigurationPlanValidator l : listeners) {
            if (!l.accept(a)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visit(BootNode a) {
        for (ReconfigurationPlanValidator l : listeners) {
            if (!l.accept(a)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visit(BootVM a) {
        for (ReconfigurationPlanValidator l : listeners) {
            if (!l.accept(a)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visit(ForgeVM a) {
        for (ReconfigurationPlanValidator l : listeners) {
            if (!l.accept(a)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visit(KillVM a) {
        for (ReconfigurationPlanValidator l : listeners) {
            if (!l.accept(a)) {
                return Boolean.FALSE;
            }

        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visit(MigrateVM a) {
        for (ReconfigurationPlanValidator l : listeners) {
            if (!l.accept(a)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visit(ResumeVM a) {
        for (ReconfigurationPlanValidator l : listeners) {
            if (!l.accept(a)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visit(ShutdownNode a) {
        for (ReconfigurationPlanValidator l : listeners) {
            if (!l.accept(a)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visit(ShutdownVM a) {
        for (ReconfigurationPlanValidator l : listeners) {
            if (!l.accept(a)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }
}
