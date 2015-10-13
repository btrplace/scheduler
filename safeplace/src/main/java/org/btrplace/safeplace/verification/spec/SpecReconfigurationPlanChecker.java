/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.safeplace.verification.spec;

import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.SatConstraintChecker;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.TimedBasedActionComparator;
import org.btrplace.plan.event.*;
import org.btrplace.safeplace.spec.prop.Proposition;

import java.util.PriorityQueue;

/**
 * Checker to verify if a reconfiguration plan satisfies a set of
 * {@link btrplace.model.constraint.SatConstraintChecker}.
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
public class SpecReconfigurationPlanChecker implements ActionVisitor {

    private boolean startingEvent = true;

    private static final TimedBasedActionComparator STARTS_CMP = new TimedBasedActionComparator(true, true);
    private static final TimedBasedActionComparator ENDS_CMP = new TimedBasedActionComparator(false, true);
    private ReconfigurationSimulator checkers;

    private ReconfigurationPlan p;

    /**
     * Make a new instance.
     */
    public SpecReconfigurationPlanChecker(Context mo, ReconfigurationPlan p) {
        checkers = new ReconfigurationSimulator(mo);
        this.p = p;
    }

    @Override
    public SatConstraint visit(Allocate a) {
        if (startingEvent) {
            checkers.start(a);
        } else {
            checkers.start(a);
        }
        return null;
    }

    @Override
    public Object visit(AllocateEvent a) {
        checkers.consume(a);
        return null;
    }

    @Override
    public SatConstraint visit(SubstitutedVMEvent a) {
        checkers.consume(a);
        return null;
    }

    @Override
    public SatConstraint visit(BootNode a) {
        if (startingEvent) {
            checkers.start(a);
        } else {
            checkers.end(a);
        }
        return null;
    }

    @Override
    public SatConstraint visit(BootVM a) {
        if (startingEvent) {
            checkers.start(a);
        } else {
            checkers.end(a);
        }
        return null;
    }

    @Override
    public SatConstraint visit(ForgeVM a) {
        if (startingEvent) {
            checkers.start(a);
        } else {
            checkers.end(a);
        }
        return null;
    }

    @Override
    public SatConstraint visit(KillVM a) {
        if (startingEvent) {
            checkers.start(a);
        } else {
            checkers.end(a);
        }
        return null;
    }

    @Override
    public SatConstraint visit(MigrateVM a) {
        if (startingEvent) {
            checkers.start(a);
        } else {
            checkers.end(a);
        }
        return null;
    }

    @Override
    public Object visit(ResumeVM a) {
        if (startingEvent) {
            checkers.start(a);
        } else {
            checkers.end(a);
        }
        return null;
    }

    @Override
    public Object visit(ShutdownNode a) {
        if (startingEvent) {
            checkers.start(a);
        } else {
            checkers.end(a);
        }
        return null;
    }

    @Override
    public Object visit(ShutdownVM a) {
        if (startingEvent) {
            checkers.start(a);
        } else {
            checkers.end(a);
        }
        return null;
    }

    @Override
    public Object visit(SuspendVM a) {
        if (startingEvent) {
            checkers.start(a);
        } else {
            checkers.end(a);
        }
        return null;
    }

    /**
     * Check if a plan satisfies all the {@link SatConstraintChecker}.
     */
    public Action check(Proposition ok) {
        if (!isConsistent(ok)) {
            throw new RuntimeException("Failure at the beginning of the plan");
        }

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
                    if (!visitAndThrowOnViolation(a, ok)) {
                        return a;
                    }
                    visitEvents(a, ok, Action.Hook.POST);
                    a = ends.peek();
                }
                a = starts.peek();

                while (a != null && a.getStart() == curMoment) {
                    starts.remove();
                    startingEvent = true;
                    visitEvents(a, ok, Action.Hook.PRE);
                    if (!visitAndThrowOnViolation(a, ok)) {
                        return a;
                    }
                    a = starts.peek();
                }
                int nextEnd = ends.isEmpty() ? Integer.MAX_VALUE : ends.peek().getEnd();
                int nextStart = starts.isEmpty() ? Integer.MAX_VALUE : starts.peek().getStart();
                curMoment = Math.min(nextEnd, nextStart);
            }
        }
        if (!isConsistent(ok)) {
            throw new RuntimeException("Failure by the end");
        }
        return null; //alright
    }

    private boolean visitAndThrowOnViolation(Action a, Proposition ok) {
        //System.out.println("Visited " + a + " on " + ok + " " + startingEvent);
        a.visit(this);
        return isConsistent(ok);
    }


    public boolean isConsistent(Proposition ok) {
        Context mo = checkers.currentModel();
        Boolean bOk = ok.eval(mo);
        return bOk;
    }

    private boolean visitEvents(Action a, Proposition ok, Action.Hook k) {
        for (Event e : a.getEvents(k)) {
            e.visit(this);
            if (!isConsistent(ok)) {
                return false;
            }
        }
        return true;
    }
}
