package btrplace.plan;

import btrplace.model.Model;
import btrplace.model.SatConstraint;

/**
 * Exception that notifies a constraint violation inside a reconfiguration plan.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanCheckerException extends Exception {

    private SatConstraint cstr;

    private Action action;

    private Model mo;

    private boolean origin = true;

    /**
     * Declare a violation caused by an action.
     *
     * @param cstr the violated constraint
     * @param a    the action provoking the violation
     */
    public ReconfigurationPlanCheckerException(SatConstraint cstr, Action a) {
        this.cstr = cstr;
        this.action = a;
    }

    /**
     * Declare a violation caused by a model.
     *
     * @param cstr  the violated constraint
     * @param model the model provoking the violation
     * @param o     {@code true} to indicate the model is the model at the origin of the plan. {@code false}
     *              to indicate the model that is reached once all the actions have been applied
     */
    public ReconfigurationPlanCheckerException(SatConstraint cstr, Model model, boolean o) {
        this.cstr = cstr;
        this.mo = model;
        this.origin = o;
    }

    /**
     * Get the violated constraint.
     *
     * @return a non-null constraint.
     */
    public SatConstraint getConstraint() {
        return cstr;
    }

    /**
     * Get the action that provoked the violation.
     *
     * @return an action. {@code null} if the violation was provoked by a model.
     */
    public Action getAction() {
        return action;
    }

    /**
     * Get the model that provoked the violation.
     *
     * @return a model. {@code null} if the violation was provoked by an action.
     */
    public Model getModel() {
        return mo;
    }

    /**
     * Indicates if a violation was provoked by the origin or the resulting model.
     * Do not consider the value when the violation is provoked by an action.
     *
     * @return {@code true} iff the violation was provoked by the origin model.
     */
    public boolean isOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        if (action != null) {
            return new StringBuilder("Action '")
                    .append(action)
                    .append("' violates the constraint '")
                    .append(cstr).toString();
        }

        StringBuilder b = new StringBuilder("The following ");
        b.append(origin ? "origin" : "resulting");
        b.append(" model violates the constraint '").append(cstr).append("':\n");
        return b.append(mo).toString();
    }
}
