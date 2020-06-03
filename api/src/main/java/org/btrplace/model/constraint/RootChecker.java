/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.plan.event.MigrateVM;

/**
 * Checker for the {@link org.btrplace.model.constraint.Root} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.Root
 */
public class RootChecker extends AllowAllConstraintChecker<Root> {

    /**
     * Make a new checker.
     *
     * @param r the associated constraint
     */
    public RootChecker(Root r) {
        super(r);
    }

    @Override
    public boolean start(MigrateVM a) {
        return !getVMs().contains(a.getVM());
    }
}
