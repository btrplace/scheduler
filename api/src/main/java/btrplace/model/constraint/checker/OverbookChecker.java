/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Overbook;
import btrplace.model.view.ShareableResource;

/**
 * Checker for the {@link btrplace.model.constraint.Overbook} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Overbook
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
        for (int nId : getNodes()) {
            if (cfg.getOnlineNodes().contains(nId)) {
                //Server capacity with the ratio
                double capa = rc.getNodeCapacity(nId) * ratio;
                //Minus the VMs usage
                for (int vmId : cfg.getRunningVMs(nId)) {
                    capa -= rc.getVMConsumption(vmId);
                    if (capa < 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
