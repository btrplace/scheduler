/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.element;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link BtrpString}
 *
 * @author Fabien Hermenier
 */
@Test
public class BtrpStringTest {

    public void testBasic() {
        BtrpString s1 = new BtrpString("foo");
        Assert.assertEquals(s1.degree(), 0);
        Assert.assertEquals(s1.type(), BtrpOperand.Type.STRING);
        Assert.assertEquals(s1.toString(), "foo");
        Assert.assertEquals(s1.prettyType(), "string");
    }

    public void testcopy() {
        BtrpString s1 = new BtrpString("foo");
        BtrpString s2 = s1.copy();
        Assert.assertEquals(s1, s2);
    }

    public void testConcatenation() {
        BtrpString s1 = new BtrpString("this");
        BtrpString res = s1.plus(new BtrpString(" is"))
                .plus(new BtrpString(" a "))
                .plus(new BtrpNumber(16, BtrpNumber.Base.BASE_16));
        Assert.assertEquals(res.toString(), "this is a 10");
    }

    public void testEq() {
        BtrpString s1 = new BtrpString("this");
        BtrpString s2 = new BtrpString("this");
        Assert.assertEquals(s1.eq(s2), BtrpNumber.TRUE);

        BtrpString s3 = new BtrpString("foo");
        Assert.assertEquals(s1.eq(s3), BtrpNumber.FALSE);

        Model mo = new DefaultModel();
        BtrpElement s = new BtrpElement(BtrpOperand.Type.VM, "1", mo.newVM());
        Assert.assertEquals(s1.eq(s), BtrpNumber.FALSE);

    }
}
