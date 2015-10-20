package org.btrplace.model.constraint.migration;

import org.btrplace.model.constraint.AllowAllConstraintChecker;

/**
 * Checker for the {@link Deadline} constraint.
 *
 * @author Vincent Kherbache
 * @see Deadline
 */
public class DeadlineChecker extends AllowAllConstraintChecker<Deadline> {

    /**
     * Make a new checker.
     *
     * @param dl the deadline constraint associated to the checker.
     */
    public DeadlineChecker(Deadline dl) {
        super(dl);
    }
}