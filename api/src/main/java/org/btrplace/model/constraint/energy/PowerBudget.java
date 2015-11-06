package org.btrplace.model.constraint.energy;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.SatConstraintChecker;

import java.util.Collections;

/**
 * Created by vins on 11/01/15.
 */
public class PowerBudget extends SatConstraint {

    private int budget, start, end;

    /**
     * Make a new constraint (continuous version).
     *
     * @param budget    power budget allowed
     * @param start     the power restriction start at t=start
     * @param end       the end of power restriction
     */
    public PowerBudget(int start, int end, int budget) {
        super(Collections.<VM>emptyList(), Collections.<Node>emptyList(), true);
        this.budget = budget;
        this.start = start;
        this.end = end;
    }

    /**
     * Make a new constraint (discrete version).
     *
     * @param budget    power budget allowed
     */
    public PowerBudget(int budget) {
        super(Collections.<VM>emptyList(), Collections.<Node>emptyList(), false);
        this.budget = budget;
    }

    public int getBudget() {
        return budget;
    }

    public int getStart() {
        if (isContinuous()) { return start; }
        else { return -1; }
    }

    public int getEnd() {
        if (isContinuous()) { return end; }
        else { return -1; }
    }

    @Override
    public boolean setContinuous(boolean b) {
        return b == isContinuous();
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new PowerBudgetChecker(this);
    }

    @Override
    public String toString() {
        return "powerBudget(" + "threshold=" + budget + ", interval=[" + start + ',' + end + "], " + restrictionToString() + ')';
    }
}
