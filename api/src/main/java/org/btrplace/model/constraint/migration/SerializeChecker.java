/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint.migration;

import org.btrplace.model.constraint.AllowAllConstraintChecker;

/**
 * Checker for the {@link Serialize} constraint.
 *
 * @author Vincent Kherbache
 * @see Serialize
 */
public class SerializeChecker extends AllowAllConstraintChecker<Serialize> {

    /**
     * Make a new checker.
     *
     * @param se the serialize constraint associated to the checker.
     */
    public SerializeChecker(Serialize se) {
        super(se);
    }
}
