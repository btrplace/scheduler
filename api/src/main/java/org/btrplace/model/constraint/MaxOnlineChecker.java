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
import org.btrplace.plan.event.ShutdownNode;

/**
 * Checker associated to the {@link MaxOnline} constraint.
 *
 * @author TU HUYNH DANG
 */
public class MaxOnlineChecker extends AllowAllConstraintChecker<MaxOnline> {

    private int currentOnline;

    /**
     * Make a new checker.
     *
     * @param cstr The associated constraint
     */
    public MaxOnlineChecker(MaxOnline cstr) {
        super(cstr);
    }

    @Override
    public boolean startsWith(Model mo) {
        if (getConstraint().isContinuous()) {
            Mapping map = mo.getMapping();
            currentOnline = 0;
            for (Node n : getConstraint().getInvolvedNodes()) {
                if (map.isOnline(n)) {
                    currentOnline++;
                }
            }
            return currentOnline <= getConstraint().getAmount();
        }
        return true;
    }

    @Override
    public boolean start(BootNode a) {
        if (getConstraint().isContinuous() && getNodes().contains(a.getNode())) {
            currentOnline++;
            return currentOnline <= getConstraint().getAmount();
        }
        return true;
    }

    @Override
    public void end(ShutdownNode a) {
        if (getNodes().contains(a.getNode())) {
            currentOnline--;
        }
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping map = mo.getMapping();
        int on = 0;
        for (Node n : getConstraint().getInvolvedNodes()) {
            if (map.isOnline(n)) {
                on++;
            }
        }
        return on <= getConstraint().getAmount();
    }
}
