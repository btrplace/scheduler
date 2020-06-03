/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan;

import org.btrplace.model.Model;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.SatConstraintChecker;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.ActionVisitor;
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.AllocateEvent;
import org.btrplace.plan.event.BootNode;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.Event;
import org.btrplace.plan.event.ForgeVM;
import org.btrplace.plan.event.KillVM;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.ResumeVM;
import org.btrplace.plan.event.ShutdownNode;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.plan.event.SubstitutedVMEvent;
import org.btrplace.plan.event.SuspendVM;
import org.btrplace.scheduler.InconsistentSolutionException;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Checker to verify if a reconfiguration plan satisfies a set of
 * {@link org.btrplace.model.constraint.SatConstraintChecker}.
 * <p>
 * In practice, the origin model is send to each of the checkers.
 * Then it notifies all the checkers for the beginning and the end moment of each of the actions and events.
 * Finally, it sends the resulting model to each of the checkers.
 * <p>
 * Actions start and end moment are notified in the increasing order of their associated moment with
 * a priority given to the end moments.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanChecker implements ActionVisitor {

    private boolean startingEvent = true;

    private static final TimedBasedActionComparator STARTS_CMP = new TimedBasedActionComparator(true, true);
  private static final TimedBasedActionComparator ENDS_CMP = new TimedBasedActionComparator(false, true);
  private final List<SatConstraintChecker<?>> checkers;

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
    public boolean addChecker(SatConstraintChecker<?> c) {
        return checkers.add(c);
    }

    /**
     * Remove a checker.
     *
     * @param c the checker to remove
     * @return {@code true} iff the checker was present
     */
    public boolean removeChecker(SatConstraintChecker<?> c) {
        return checkers.remove(c);
    }

    @Override
    public SatConstraint visit(Allocate a) {
        for (SatConstraintChecker<?> c : checkers) {
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
        for (SatConstraintChecker<?> c : checkers) {
            if (!c.consume(a)) {
                return c.getConstraint();
            }
        }
        return null;
    }

    @Override
    public SatConstraint visit(SubstitutedVMEvent a) {
        for (SatConstraintChecker<?> c : checkers) {
            if (!c.consume(a)) {
                return c.getConstraint();
            }
        }
        return null;
    }

    @Override
    public SatConstraint visit(BootNode a) {
        for (SatConstraintChecker<?> c : checkers) {
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
        for (SatConstraintChecker<?> c : checkers) {
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
        for (SatConstraintChecker<?> c : checkers) {
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
        for (SatConstraintChecker<?> c : checkers) {
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
        for (SatConstraintChecker<?> c : checkers) {
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
        for (SatConstraintChecker<?> c : checkers) {
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
        for (SatConstraintChecker<?> c : checkers) {
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
        for (SatConstraintChecker<?> c : checkers) {
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
        for (SatConstraintChecker<?> c : checkers) {
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
     * Check if a plan satisfies all the {@link SatConstraintChecker}.
     *
     * @param p the plan to check
     * @throws SatConstraintViolationException if a violation is detected
     */
    public void check(ReconfigurationPlan p) throws SatConstraintViolationException {
        if (checkers.isEmpty()) {
            return;
        }

        checkModel(p.getOrigin(), true);

        if (!p.getActions().isEmpty()) {
            PriorityQueue<Action> starts = new PriorityQueue<>(p.getActions().size(), STARTS_CMP);
            PriorityQueue<Action> ends = new PriorityQueue<>(p.getActions().size(), ENDS_CMP);
            starts.addAll(p.getActions());
            ends.addAll(p.getActions());

            //Starts the actions
            int curMoment = starts.peek().getStart();
            while (!starts.isEmpty() || !ends.isEmpty()) {
                Action a = ends.peek();
                while (a != null && a.getEnd() == curMoment) {
                    ends.remove();
                    startingEvent = false;
                    visitAndThrowOnViolation(a);
                    visitEvents(a, Action.Hook.POST);
                    a = ends.peek();
                }
                a = starts.peek();

                while (a != null && a.getStart() == curMoment) {
                    starts.remove();
                    startingEvent = true;
                    visitEvents(a, Action.Hook.PRE);
                    visitAndThrowOnViolation(a);
                    a = starts.peek();
                }
                int nextEnd = Integer.MAX_VALUE;
                if (!ends.isEmpty()) {
                    nextEnd = ends.peek().getEnd();
                }
                int nextStart = Integer.MAX_VALUE;
                if (!starts.isEmpty()) {
                    nextStart = starts.peek().getStart();
                }

                curMoment = Math.min(nextEnd, nextStart);
            }
        }
        Model mo = p.getResult();
        if (mo == null) {
          throw new InconsistentSolutionException(p, "The resulting reconfiguration plan is not applyable");
        }
        checkModel(mo, false);
    }

    private void visitAndThrowOnViolation(Action a) throws SatConstraintViolationException {
        SatConstraint c = (SatConstraint) a.visit(this);
        if (c != null) {
            throw new ContinuousViolationException(c, a);
        }
    }

    private void visitEvents(Action a, Action.Hook k) throws SatConstraintViolationException {
        SatConstraint c;
        for (Event e : a.getEvents(k)) {
            c = (SatConstraint) e.visit(this);
            if (c != null) {
                throw new ContinuousViolationException(c, a);
            }
        }
    }

    /**
     * Check for the validity of a model.
     *
     * @param mo    the model to check
     * @param start {@code true} iff the model corresponds to the origin model. Otherwise it is considered
     *              to be the resulting model
     * @throws SatConstraintViolationException if at least one constraint is violated.
     */
    private void checkModel(Model mo, boolean start) throws SatConstraintViolationException {
        for (SatConstraintChecker<?> c : checkers) {
            if (start && !c.startsWith(mo)) {
                SatConstraint cs = c.getConstraint();
                if (cs != null) {
                    throw new DiscreteViolationException(c.getConstraint(), mo);
                }
            } else if (!start && !c.endsWith(mo)) {
                SatConstraint cs = c.getConstraint();
                if (cs != null) {
                    throw new DiscreteViolationException(c.getConstraint(), mo);
                }
            }
        }
    }
}
