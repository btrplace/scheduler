package btrplace.plan;

import btrplace.model.Model;
import btrplace.plan.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author Fabien Hermenier
 */
public class MomentBasedPlanApplier extends DefaultPlanApplier implements ActionVisitor {

    private static final TimedBasedActionComparator startsCmp = new TimedBasedActionComparator(true, true);
    private static final TimedBasedActionComparator endsCmp = new TimedBasedActionComparator(false, true);
    private List<ReconfigurationPlanChecker> checkers;

    public MomentBasedPlanApplier() {
        checkers = new ArrayList<>();
    }

    public void addChecker(ReconfigurationPlanChecker c) {
        checkers.add(c);
    }

    public void removeChecker(ReconfigurationPlanChecker c) {
        checkers.remove(c);
    }

    @Override
    public Object visit(Allocate a) {
        for (ReconfigurationPlanChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return Boolean.FALSE;
                }
            } else {
                c.end(a);
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(AllocateEvent a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(SubstitutedVMEvent a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(BootNode a) {
        for (ReconfigurationPlanChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return Boolean.FALSE;
                }
            } else {
                c.end(a);
            }
        }
        return Boolean.TRUE;

    }

    @Override
    public Object visit(BootVM a) {
        for (ReconfigurationPlanChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return Boolean.FALSE;
                }
            } else {
                c.end(a);
            }
        }
        return Boolean.TRUE;

    }

    @Override
    public Object visit(ForgeVM a) {
        for (ReconfigurationPlanChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return Boolean.FALSE;
                }
            } else {
                c.end(a);
            }
        }
        return Boolean.TRUE;

    }

    @Override
    public Object visit(KillVM a) {
        for (ReconfigurationPlanChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return Boolean.FALSE;
                }
            } else {
                c.end(a);
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(MigrateVM a) {
        for (ReconfigurationPlanChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return Boolean.FALSE;
                }
            } else {
                c.end(a);
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(ResumeVM a) {
        for (ReconfigurationPlanChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return Boolean.FALSE;
                }
            } else {
                c.end(a);
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(ShutdownNode a) {
        for (ReconfigurationPlanChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return Boolean.FALSE;
                }
            } else {
                c.end(a);
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(ShutdownVM a) {
        for (ReconfigurationPlanChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return Boolean.FALSE;
                }
            } else {
                c.end(a);
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(SuspendVM a) {
        for (ReconfigurationPlanChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return Boolean.FALSE;
                }
            } else {
                c.end(a);
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public String toString(ReconfigurationPlan p) {
        throw new UnsupportedOperationException();
    }


    private boolean startingEvent = true;

    @Override
    public Model apply(ReconfigurationPlan p) {
        PriorityQueue<Action> starts = new PriorityQueue<>(p.getActions().size(), startsCmp);
        PriorityQueue<Action> ends = new PriorityQueue<>(p.getActions().size(), endsCmp);
        starts.addAll(p.getActions());
        ends.addAll(p.getActions());

        //Starts the actions
        int curMoment = starts.peek().getStart();
        Model mo = p.getOrigin().clone();
        while (!starts.isEmpty() && !ends.isEmpty()) {
            Action a = ends.peek();
            while (a.getEnd() == curMoment) {
                ends.remove();
                startingEvent = false;
                if (!Boolean.FALSE == a.visit(this)) {
                    return null;
                }
                if (!a.apply(mo)) {
                    return null;
                }
            }

            a = ends.peek();
            while (a.getStart() == curMoment) {
                starts.remove();
                startingEvent = true;
                if (!Boolean.FALSE == a.visit(this)) {
                    return null;
                }
            }
            curMoment = Math.min(ends.peek().getEnd(), starts.peek().getStart());
        }
        return mo;
    }

}
