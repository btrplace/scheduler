package btrplace.plan;

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.checker.SatConstraintChecker;
import btrplace.plan.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Checker to verify if a reconfiguration plan satisfies a set of {@link btrplace.model.constraint.checker.SatConstraintChecker}.
 * <p/>
 * In practice, the origin model is sends to each of the checkers.
 * Then it notifies all the checker for the consume then the end moment of each of the action and event.
 * Finally, it sends the resulting model to each of the checkers.
 * <p/>
 * Action consume and end moment are notified in the increasing order of their associated moment.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanChecker implements ActionVisitor {

    private boolean startingEvent = true;

    private static final TimedBasedActionComparator startsCmp = new TimedBasedActionComparator(true, true);
    private static final TimedBasedActionComparator endsCmp = new TimedBasedActionComparator(false, true);
    private List<SatConstraintChecker> checkers;

    /**
     * Make a new instance.
     */
    public ReconfigurationPlanChecker() {
        checkers = new ArrayList<>();
    }

    /**
     * Add an additional checker.
     *
     * @param c the checker to add
     * @return {@code true} iff the checker has been added
     */
    public boolean addChecker(SatConstraintChecker c) {
        return checkers.add(c);
    }

    /**
     * Remove a checker.
     *
     * @param c the checker to remove
     * @return {@code true} iff the checker was present
     */
    public boolean removeChecker(SatConstraintChecker c) {
        return checkers.remove(c);
    }

    @Override
    public SatConstraint visit(Allocate a) {
        for (SatConstraintChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return c.getConstraint();
                }
            } else {
                c.end(a);
            }
        }
        return null;
    }

    @Override
    public Object visit(AllocateEvent a) {
        for (SatConstraintChecker c : checkers) {
            if (!c.consume(a)) {
                return c.getConstraint();
            }
        }
        return null;
    }

    @Override
    public SatConstraint visit(SubstitutedVMEvent a) {
        for (SatConstraintChecker c : checkers) {
            if (!c.consume(a)) {
                return c.getConstraint();
            }
        }
        return null;
    }

    @Override
    public SatConstraint visit(BootNode a) {
        for (SatConstraintChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return c.getConstraint();
                }
            } else {
                c.end(a);
            }
        }
        return null;

    }

    @Override
    public SatConstraint visit(BootVM a) {
        for (SatConstraintChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return c.getConstraint();
                }
            } else {
                c.end(a);
            }
        }
        return null;

    }

    @Override
    public SatConstraint visit(ForgeVM a) {
        for (SatConstraintChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return c.getConstraint();
                }
            } else {
                c.end(a);
            }
        }
        return null;

    }

    @Override
    public SatConstraint visit(KillVM a) {
        for (SatConstraintChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return c.getConstraint();
                }
            } else {
                c.end(a);
            }
        }
        return null;
    }

    @Override
    public SatConstraint visit(MigrateVM a) {
        for (SatConstraintChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return c.getConstraint();
                }
            } else {
                c.end(a);
            }
        }
        return null;
    }

    @Override
    public Object visit(ResumeVM a) {
        for (SatConstraintChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return c.getConstraint();
                }
            } else {
                c.end(a);
            }
        }
        return null;
    }

    @Override
    public Object visit(ShutdownNode a) {
        for (SatConstraintChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return c.getConstraint();
                }
            } else {
                c.end(a);
            }
        }
        return null;
    }

    @Override
    public Object visit(ShutdownVM a) {
        for (SatConstraintChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return c.getConstraint();
                }
            } else {
                c.end(a);
            }
        }
        return null;
    }

    @Override
    public Object visit(SuspendVM a) {
        for (SatConstraintChecker c : checkers) {
            if (startingEvent) {
                if (!c.start(a)) {
                    return c.getConstraint();
                }
            } else {
                c.end(a);
            }
        }
        return null;
    }

    /**
     * Check if a plan satisfies all the stated {@link SatConstraintChecker}.
     *
     * @param p the plan to check
     * @throws ReconfigurationPlanCheckerException
     *          if a violation is detected
     */
    public void check(ReconfigurationPlan p) throws ReconfigurationPlanCheckerException {
        if (checkers.isEmpty() || p.getActions().isEmpty()) {
            return;
        }
        PriorityQueue<Action> starts = new PriorityQueue<>(p.getActions().size(), startsCmp);
        PriorityQueue<Action> ends = new PriorityQueue<>(p.getActions().size(), endsCmp);
        starts.addAll(p.getActions());
        ends.addAll(p.getActions());

        //Starts the actions
        int curMoment = starts.peek().getStart();
        SatConstraint c = checkModel(p.getOrigin(), true);
        if (c != null) {
            throw new ReconfigurationPlanCheckerException(c, p.getOrigin(), true);
        }
        while (!starts.isEmpty() || !ends.isEmpty()) {
            Action a = ends.peek();
            while (a != null && a.getEnd() == curMoment) {
                ends.remove();
                startingEvent = false;
                c = (SatConstraint) a.visit(this);
                if (c != null) {
                    throw new ReconfigurationPlanCheckerException(c, a);
                }
                for (Event e : a.getEvents(Action.Hook.post)) {
                    c = (SatConstraint) e.visit(this);
                    if (c != null) {
                        throw new ReconfigurationPlanCheckerException(c, a);
                    }
                }

                a = ends.peek();
            }

            a = starts.peek();
            while (a != null && a.getStart() == curMoment) {
                starts.remove();
                startingEvent = true;
                for (Event e : a.getEvents(Action.Hook.pre)) {
                    c = (SatConstraint) e.visit(this);
                    if (c != null) {
                        throw new ReconfigurationPlanCheckerException(c, a);
                    }
                }
                c = (SatConstraint) a.visit(this);
                if (c != null) {
                    throw new ReconfigurationPlanCheckerException(c, a);
                }
                a = starts.peek();
            }
            int nextEnd = ends.isEmpty() ? Integer.MAX_VALUE : ends.peek().getEnd();
            int nextStart = starts.isEmpty() ? Integer.MAX_VALUE : starts.peek().getStart();
            curMoment = Math.min(nextEnd, nextStart);
        }
        Model mo = p.getResult();
        c = checkModel(mo, false);
        if (c != null) {
            throw new ReconfigurationPlanCheckerException(c, mo, false);
        }

    }

    /**
     * Check for the validity of a model.
     *
     * @param mo    the model to check
     * @param start {@code true} iff the model corresponds to the origin model. Otherwise it is considered
     *              to be the resulting model
     * @return the first violated constraint or {@code null} if no constraint is violated
     */
    public SatConstraint checkModel(Model mo, boolean start) {
        for (SatConstraintChecker c : checkers) {
            if (start && !c.startsWith(mo)) {
                return c.getConstraint();
            } else if (!start && !c.endsWith(mo)) {
                return c.getConstraint();
            }
        }
        return null;
    }
}
