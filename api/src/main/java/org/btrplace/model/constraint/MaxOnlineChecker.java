/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
            return (currentOnline <= getConstraint().getAmount());
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
