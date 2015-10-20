package org.btrplace.model.constraint.migration;

import org.btrplace.model.constraint.AllowAllConstraintChecker;

/**
 * Checker for the {@link Precedence} constraint.
 *
 * @author Vincent Kherbache
 * @see Precedence
 */
public class PrecedenceChecker extends AllowAllConstraintChecker<Precedence> {

    /**
     * Make a new checker.
     *
     * @param p the precedence constraint associated to the checker.
     */
    public PrecedenceChecker(Precedence p) {
        super(p);
    }

}
