/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package btrplace.solver.api.cstrSpec.verification.spec;

import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.checker.SatConstraintChecker;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.ReconfigurationPlanCheckerException;
import btrplace.plan.TimedBasedActionComparator;
import btrplace.plan.event.*;
import btrplace.solver.api.cstrSpec.spec.prop.Proposition;

import java.util.PriorityQueue;

/**
 * Checker to verify if a reconfiguration plan satisfies a set of
 * {@link btrplace.model.constraint.checker.SatConstraintChecker}.
 * <p/>
 * In practice, the origin model is send to each of the checkers.
 * Then it notifies all the checkers for the beginning and the end moment of each of the actions and events.
 * Finally, it sends the resulting model to each of the checkers.
 * <p/>
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

    /**
     * Make a new instance.
     */
    public SpecReconfigurationPlanChecker() {
        checkers = new ReconfigurationSimulator();
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
     *
     * @param p the plan to check
     * @throws ReconfigurationPlanCheckerException if a violation is detected
     */
    public void check(ReconfigurationPlan p, Proposition ok, Proposition ko) throws ReconfigurationPlanCheckerException {
        checkModel(new SpecModel(p.getOrigin()), true);

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
                    visitAndThrowOnViolation(a, ok, ko);
                    visitEvents(a, Action.Hook.post);
                    a = ends.peek();
                }
                a = starts.peek();

                while (a != null && a.getStart() == curMoment) {
                    starts.remove();
                    startingEvent = true;
                    visitEvents(a, Action.Hook.pre);
                    visitAndThrowOnViolation(a, ok, ko);
                    a = starts.peek();
                }
                int nextEnd = ends.isEmpty() ? Integer.MAX_VALUE : ends.peek().getEnd();
                int nextStart = starts.isEmpty() ? Integer.MAX_VALUE : starts.peek().getStart();
                curMoment = Math.min(nextEnd, nextStart);
            }
        }
        SpecModel mo = checkers.currentModel();
        checkModel(mo, false);
    }

    private void visitAndThrowOnViolation(Action a, Proposition ok, Proposition ko) throws ReconfigurationPlanCheckerException {
        a.visit(this);
        SpecModel mo = checkers.currentModel();
        Boolean bOk = ok.eval(mo);
        Boolean bKo = ko.eval(mo);
        if (bOk == null || bKo == null) {
            throw new RuntimeException(ok.eval(mo) + "\n" + ko.eval(mo));
        }
        if (bOk.equals(bKo)) {
            throw new RuntimeException("Both have the same result: " + bOk + " " + bKo);
        }
        if (!bOk) {
            throw new ReconfigurationPlanCheckerException(null, a);
        }
    }


    private void visitEvents(Action a, Action.Hook k) throws ReconfigurationPlanCheckerException {
        SatConstraint c;
        for (Event e : a.getEvents(k)) {
            c = (SatConstraint) e.visit(this);
            if (c != null) {
                throw new ReconfigurationPlanCheckerException(c, a);
            }
        }
    }

    /**
     * Check for the validity of a model.
     *
     * @param mo    the model to check
     * @param start {@code true} iff the model corresponds to the origin model. Otherwise it is considered
     *              to be the resulting model
     * @throws ReconfigurationPlanCheckerException if at least one constraint is violated.
     */
    private void checkModel(SpecModel mo, boolean start) {
        if (start) {
            checkers.startsWith(mo);
        } else {
            checkers.endsWith(mo);
        }
    }
}
