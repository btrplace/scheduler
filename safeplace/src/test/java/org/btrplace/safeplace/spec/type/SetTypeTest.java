/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.type;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class SetTypeTest {

    @Test
    public void testSimple() {
        SetType t = new SetType(IntType.getInstance());
        System.out.println(t);
        Assert.assertEquals(t.enclosingType(), IntType.getInstance());
        SetType t2 = new SetType(t);
        Assert.assertEquals(t2.enclosingType(), t);
        System.out.println(t2);
    }
}
