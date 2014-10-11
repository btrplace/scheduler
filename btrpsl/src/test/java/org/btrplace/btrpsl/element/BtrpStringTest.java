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
        Assert.assertEquals(s1.type(), BtrpOperand.Type.string);
        Assert.assertEquals(s1.toString(), "foo");
        Assert.assertEquals(s1.prettyType(), "string");
    }

    public void testClone() {
        BtrpString s1 = new BtrpString("foo");
        BtrpString s2 = s1.clone();
        Assert.assertEquals(s1, s2);
    }

    public void testConcatenation() {
        BtrpString s1 = new BtrpString("this");
        BtrpString res = s1.plus(new BtrpString(" is"))
                .plus(new BtrpString(" a "))
                .plus(new BtrpNumber(16, BtrpNumber.Base.base16));
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
