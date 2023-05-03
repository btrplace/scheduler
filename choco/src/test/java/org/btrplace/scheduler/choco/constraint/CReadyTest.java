/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.*;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Ready;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * Unit tests for {@link org.btrplace.scheduler.choco.constraint.CReady}.
 *
 * @author Fabien Hermenier
 */
public class CReadyTest {

    @Test
    public void testGetMisplaced() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        mo.getMapping().ready(vm1).on(n1).run(n1, vm2, vm3);
        Instance i = new Instance(mo, Collections.emptyList(), new MinMTTR());
        CReady k = new CReady(new Ready(vm1));
        Assert.assertEquals(k.getMisPlacedVMs(i).size(), 0);

        k = new CReady(new Ready(vm2));
        Assert.assertEquals(k.getMisPlacedVMs(i).size(), 1);
        Assert.assertTrue(k.getMisPlacedVMs(i).contains(vm2));
    }
}
