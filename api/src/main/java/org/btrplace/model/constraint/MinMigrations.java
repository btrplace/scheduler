/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

/**
 * An objective that just minimizes the cumulative duration of the migrations
 * to perform during a reconfiguration.
 *
 * Contrarily to {@link MinMTTR}, it only focuses on migration and do not consider
 * the possible action delay.
 *
 * @author Fabien Hermenier
 */
public class MinMigrations extends OptConstraint {

    @Override
    public String id() {
        return "minMigrations";
    }
}
