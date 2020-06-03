/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link Slice}.
 *
 * @author Fabien Hermenier
 */
public class SliceTest {

    /**
     * It's a container so we only tests the instantiation and the getters.
     */
    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        org.chocosolver.solver.Model m = new org.chocosolver.solver.Model("");
        IntVar st = m.intVar("start", 1);
        IntVar ed = m.intVar("end", 3);
        IntVar duration = m.intVar("duration", 2);
        IntVar hoster = m.intVar("hoster", 4);
        Slice sl = new Slice(vm1, st, ed, duration, hoster);
        Assert.assertEquals(vm1, sl.getSubject());
        Assert.assertEquals(st, sl.getStart());
        Assert.assertEquals(ed, sl.getEnd());
        Assert.assertEquals(hoster, sl.getHoster());
        Assert.assertEquals(duration, sl.getDuration());
        Assert.assertFalse(sl.toString().contains("null"));
        duration = m.intVar("duration", 3, 5, true);
        sl = new Slice(vm1, st, ed, duration, hoster);
        Assert.assertFalse(sl.toString().contains("null"));
    }
}
