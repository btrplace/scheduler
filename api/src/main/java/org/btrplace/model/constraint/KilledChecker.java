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
import org.btrplace.model.VM;
import org.btrplace.plan.event.KillVM;

/**
 * Checker for the {@link org.btrplace.model.constraint.Killed} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.Killed
 */
public class KilledChecker extends DenyMyVMsActions<Killed> {

    /**
     * Make a new checker.
     *
     * @param k the associated constraint
     */
    public KilledChecker(Killed k) {
        super(k);
    }

    @Override
    public boolean start(KillVM a) {
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (VM vm : getVMs()) {
            if (c.getAllVMs().contains(vm)) {
                return false;
            }
        }
        return true;
    }
}
