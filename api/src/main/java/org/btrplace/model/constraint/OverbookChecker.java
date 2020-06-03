/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

  private final String id;

  private final double ratio;

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
        ShareableResource rc = ShareableResource.get(i, id);
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
