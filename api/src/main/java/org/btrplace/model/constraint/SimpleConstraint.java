package org.btrplace.model.constraint;

/**
 * A skeleton for a constraint that can be either discrete or continuous.
 *
 * @author Fabien Hermenier
 */
public abstract class SimpleConstraint implements SatConstraint {

    private boolean continuous;

    /**
     * Build a new constraint.
     *
     * @param continuous {@code true} to state a continuous constraint. {@code false} for a discrete one
     */
    public SimpleConstraint(boolean continuous) {
        this.continuous = continuous;
    }

    @Override
    public boolean isContinuous() {
        return continuous;
    }

    @Override
    public boolean setContinuous(boolean b) {
        this.continuous = b;
        return true;
    }
}
