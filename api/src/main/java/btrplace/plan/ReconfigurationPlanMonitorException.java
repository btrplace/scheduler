package btrplace.plan;

import btrplace.model.Model;

/**
 * An exception that occur when an action commit failed.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanMonitorException extends Exception {

    private Model mo;

    private Action a;

    public ReconfigurationPlanMonitorException(Model m, Action a, String msg) {
        super(msg);
        mo = m;
        this.a = a;
    }

    public Model getModel() {
        return mo;
    }

    public Action getAction() {
        return a;
    }
}
