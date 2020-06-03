/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

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
