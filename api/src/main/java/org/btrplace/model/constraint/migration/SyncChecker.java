/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint.migration;

import org.btrplace.model.constraint.AllowAllConstraintChecker;

/**
 * Checker for the {@link Sync} constraint.
 *
 * @author Vincent Kherbache
 * @see Sync
 */
public class SyncChecker extends AllowAllConstraintChecker<Sync> {

    /**
     * Make a new checker.
     *
     * @param se the sync constraint associated to the checker.
     */
    public SyncChecker(Sync se) {
        super(se);
    }
}
