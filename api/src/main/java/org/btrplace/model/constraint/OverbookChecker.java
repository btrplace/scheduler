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
import org.btrplace.model.VM;
import org.btrplace.model.view.ShareableResource;

/**
 * Checker for the {@link org.btrplace.model.constraint.Overbook} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.Overbook
 */
public class OverbookChecker extends AllowAllConstraintChecker<Overbook> {

    private String id;

    private double ratio;

    /**
     * Make a new checker.
     *
     * @param o the associated constraint
     */
    public OverbookChecker(Overbook o) {
        super(o);
        id = o.getResource();
        ratio = o.getRatio();
    }

    @Override
    public boolean endsWith(Model i) {
        Mapping cfg = i.getMapping();
        ShareableResource rc = (ShareableResource) i.getView(ShareableResource.VIEW_ID_BASE + id);
        if (rc == null) {
            return false;
        }
        for (Node nId : getNodes()) {
            if (cfg.isOnline(nId)) {
                //Server capacity with the ratio
                double c = rc.getCapacity(nId) * ratio;
                //Minus the VMs usage
                for (VM vmId : cfg.getRunningVMs(nId)) {
                    c -= rc.getConsumption(vmId);
                    if (c < 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
