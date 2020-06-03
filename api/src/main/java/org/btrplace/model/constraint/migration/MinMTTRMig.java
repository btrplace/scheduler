/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint.migration;

import org.btrplace.model.constraint.OptConstraint;

/**
 * An optimization constraint that minimizes the time to repair a non-viable model involving a set of migrations.
 * In practice it minimizes the sum of the ending moment for each needed actions.
 *
 * @author Vincent Kherbache
 */
public class MinMTTRMig extends OptConstraint {

    @Override
    public String id() {
        return "minimizeMTTRMigrationScheduling";
    }
}
