/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.plan.event.BootNode;

/**
 * Checker for the {@link org.btrplace.model.constraint.Offline} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.Offline
 */
public class OfflineChecker extends AllowAllConstraintChecker<Offline> {

    /**
     * Make a new checker.
     *
     * @param o the associated constraint
     */
    public OfflineChecker(Offline o) {
        super(o);
    }

    @Override
    public boolean start(BootNode a) {
        return !getNodes().contains(a.getNode());
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (Node n : getNodes()) {
            if (!c.isOffline(n)) {
                return false;
            }
        }
        return true;
    }
}
