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
 * Unit tests for {@link org.btrplace.btrpsl.element.BtrpNumber}
 *
 * @author Fabien Hermenier
 */
@Test
public class BtrpNumberTest {

    private static void checkContent(BtrpNumber v, int val, BtrpNumber.Base base) {
        Assert.assertEquals(v.getIntValue(), val);
        Assert.assertTrue(v.isInteger());
        Assert.assertEquals(v.getBase(), base);
    }

    private static void checkContent(BtrpNumber v, double val) {
        Assert.assertEquals(v.getDoubleValue(), val);
        Assert.assertFalse(v.isInteger());
        Assert.assertEquals(v.getBase(), BtrpNumber.Base.base10);
    }

    private static void checkTrue(BtrpNumber v) {
        Assert.assertTrue(v.isInteger());
        Assert.assertEquals(v, BtrpNumber.TRUE);
    }

    private static void checkFalse(BtrpNumber v) {
        Assert.assertTrue(v.isInteger());
        Assert.assertEquals(v, BtrpNumber.FALSE);
    }


    public void testInstantiation() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base10);
        Assert.assertEquals(i.type(), BtrpOperand.Type.number);
        Assert.assertEquals(i.degree(), 0);
        Assert.assertEquals(i.prettyType(), "number");
        checkContent(i, 5, BtrpNumber.Base.base10);

        i = new BtrpNumber(7.5);
        Assert.assertEquals(i.type(), BtrpOperand.Type.number);
        Assert.assertEquals(i.degree(), 0);
        checkContent(i, 7.5);
    }

    public void testToString() {
        Assert.assertEquals(new BtrpNumber(17, BtrpNumber.Base.base10).toString(), "17");
        Assert.assertEquals(new BtrpNumber(17, BtrpNumber.Base.base8).toString(), "21");
        Assert.assertEquals(new BtrpNumber(17, BtrpNumber.Base.base16).toString(), "11");
        Assert.assertEquals(new BtrpNumber(21.4).toString(), "21.4");
    }

    public void testViablePower() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        BtrpNumber j = new BtrpNumber(3, BtrpNumber.Base.base10);
        BtrpNumber k = i.power(j);
        checkContent(k, 125, BtrpNumber.Base.base16);

        checkContent(new BtrpNumber(25, BtrpNumber.Base.base10).power(new BtrpNumber(0.5)), 5);
        checkContent(new BtrpNumber(2.5).power(new BtrpNumber(2, BtrpNumber.Base.base10)), 6.25);
        checkContent(new BtrpNumber(6.25).power(new BtrpNumber(0.5)), 2.5);
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViablePower() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        Model mo = new DefaultModel();
        BtrpElement j = new BtrpElement(BtrpOperand.Type.VM, "foo", mo.newVM());
        i.power(j);
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViablePower2() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        BtrpNumber j = new BtrpNumber(-3, BtrpNumber.Base.base16);
        i.power(j);
    }

    public void testViableAddition() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        BtrpNumber j = new BtrpNumber(3, BtrpNumber.Base.base16);
        BtrpNumber k = i.plus(j);
        checkContent(k, 8, BtrpNumber.Base.base16);

        checkContent(new BtrpNumber(2.5).plus(new BtrpNumber(2.5)), 5.0);
        checkContent(new BtrpNumber(2.5).plus(new BtrpNumber(2, BtrpNumber.Base.base10)), 4.5);
        checkContent(new BtrpNumber(2, BtrpNumber.Base.base10).plus(new BtrpNumber(2.5)), 4.5);
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableAddition() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        Model mo = new DefaultModel();
        BtrpElement j = new BtrpElement(BtrpOperand.Type.VM, "foo", mo.newVM());
        i.plus(j);
    }

    public void testViableDifference() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        BtrpNumber j = new BtrpNumber(3, BtrpNumber.Base.base16);
        BtrpNumber k = j.minus(i);
        checkContent(k, -2, BtrpNumber.Base.base16);

        checkContent(new BtrpNumber(2.5).minus(new BtrpNumber(2.5)), 0);
        checkContent(new BtrpNumber(2.5).minus(new BtrpNumber(2, BtrpNumber.Base.base10)), 0.5);
        checkContent(new BtrpNumber(2, BtrpNumber.Base.base10).minus(new BtrpNumber(2.5)), -0.5);

    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableDifference() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        Model mo = new DefaultModel();
        BtrpElement j = new BtrpElement(BtrpOperand.Type.VM, "foo", mo.newVM());
        i.minus(j);
    }

    public void testNegate() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        BtrpNumber k = i.negate();
        checkContent(k, -5, BtrpNumber.Base.base16);

        checkContent(new BtrpNumber(5.7).negate(), -5.7);
    }

    public void testViableTimes() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        BtrpNumber j = new BtrpNumber(3, BtrpNumber.Base.base16);
        BtrpNumber k = i.times(j);
        checkContent(k, 15, BtrpNumber.Base.base16);

        checkContent(new BtrpNumber(0.5).times(new BtrpNumber(0.5)), 0.25);
        checkContent(new BtrpNumber(2.5).plus(new BtrpNumber(2, BtrpNumber.Base.base10)), 4.5);
        checkContent(new BtrpNumber(2, BtrpNumber.Base.base10).times(new BtrpNumber(-2.5)), -5);

    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableTimes() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        Model mo = new DefaultModel();
        BtrpElement j = new BtrpElement(BtrpOperand.Type.VM, "foo", mo.newVM());
        i.times(j);
    }

    public void testViableDiv() {
        BtrpNumber i = new BtrpNumber(18, BtrpNumber.Base.base16);
        BtrpNumber j = new BtrpNumber(3, BtrpNumber.Base.base16);
        BtrpNumber k = i.div(j);
        checkContent(k, 6, BtrpNumber.Base.base16);

        checkContent(new BtrpNumber(2.5).div(new BtrpNumber(2.5)), 1);
        checkContent(new BtrpNumber(2.5).div(new BtrpNumber(2, BtrpNumber.Base.base10)), 1.25);
        checkContent(new BtrpNumber(5, BtrpNumber.Base.base10).div(new BtrpNumber(-2.5)), -2);
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableDiv() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        Model mo = new DefaultModel();
        BtrpElement j = new BtrpElement(BtrpOperand.Type.VM, "foo", mo.newVM());
        i.div(j);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testNonViableDivCauseZero() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        i.div(new BtrpNumber(0, BtrpNumber.Base.base16));
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testNonViableDivCauseRealZero() {
        BtrpNumber i = new BtrpNumber(5.7);
        i.div(new BtrpNumber(0.0));
    }

    public void testViableRemainder() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        BtrpNumber j = new BtrpNumber(3, BtrpNumber.Base.base16);
        BtrpNumber k = i.remainder(j);
        checkContent(k, 2, BtrpNumber.Base.base16);

        checkContent(new BtrpNumber(2.5).remainder(new BtrpNumber(2.5)), 0.0);
        checkContent(new BtrpNumber(5.5).remainder(new BtrpNumber(2, BtrpNumber.Base.base10)), 1.5);
        checkContent(new BtrpNumber(5, BtrpNumber.Base.base10).remainder(new BtrpNumber(2.5)), 0);
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableRemainder() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        Model mo = new DefaultModel();
        BtrpElement j = new BtrpElement(BtrpOperand.Type.VM, "foo", mo.newVM());
        i.remainder(j);
    }

    public void testViableEq() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        BtrpNumber j = new BtrpNumber(5, BtrpNumber.Base.base16);

        checkTrue(i.eq(j));
        BtrpNumber l = new BtrpNumber(2, BtrpNumber.Base.base16);
        checkFalse(i.eq(l));

        checkTrue(new BtrpNumber(2.5).eq(new BtrpNumber(2.5)));
        checkTrue(new BtrpNumber(2.0).eq(new BtrpNumber(2, BtrpNumber.Base.base10)));
        checkFalse(new BtrpNumber(5, BtrpNumber.Base.base10).eq(new BtrpNumber(7.2)));

    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableEq() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        Model mo = new DefaultModel();
        BtrpElement j = new BtrpElement(BtrpOperand.Type.VM, "foo", mo.newVM());
        i.remainder(j);
    }

    public void testViableGeq() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        BtrpNumber j = new BtrpNumber(3, BtrpNumber.Base.base16);
        checkTrue(i.geq(j));
        BtrpNumber l = new BtrpNumber(7, BtrpNumber.Base.base16);
        checkFalse(i.geq(l));
        checkTrue(i.geq(i));

        checkTrue(new BtrpNumber(2.5).geq(new BtrpNumber(2.5)));
        checkTrue(new BtrpNumber(2.5).geq(new BtrpNumber(2, BtrpNumber.Base.base10)));
        checkFalse(new BtrpNumber(5, BtrpNumber.Base.base10).geq(new BtrpNumber(7.2)));

    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableGeq() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        Model mo = new DefaultModel();
        BtrpElement j = new BtrpElement(BtrpOperand.Type.VM, "foo", mo.newVM());
        i.remainder(j);
    }

    public void testViableGt() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        BtrpNumber j = new BtrpNumber(3, BtrpNumber.Base.base16);
        checkTrue(i.gt(j));
        BtrpNumber l = new BtrpNumber(5, BtrpNumber.Base.base16);
        checkFalse(i.gt(l));

        checkTrue(new BtrpNumber(2.5).gt(new BtrpNumber(2.4)));
        checkTrue(new BtrpNumber(2.5).gt(new BtrpNumber(2, BtrpNumber.Base.base10)));
        checkFalse(new BtrpNumber(5, BtrpNumber.Base.base10).gt(new BtrpNumber(7.2)));
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableGt() {
        BtrpNumber i = new BtrpNumber(5, BtrpNumber.Base.base16);
        Model mo = new DefaultModel();
        BtrpElement j = new BtrpElement(BtrpOperand.Type.VM, "foo", mo.newVM());
        i.gt(j);
    }

    public void testNot() {
        BtrpNumber i = new BtrpNumber(0, BtrpNumber.Base.base16);
        Assert.assertEquals(i.not(), BtrpNumber.TRUE);
        i = new BtrpNumber(1, BtrpNumber.Base.base16);
        Assert.assertEquals(i.not(), BtrpNumber.FALSE);
        i = new BtrpNumber(5, BtrpNumber.Base.base16);
        Assert.assertEquals(i.not(), BtrpNumber.FALSE);
        i = new BtrpNumber(2.5);
        Assert.assertEquals(i.not(), BtrpNumber.FALSE);
    }

    public void testClone() {
        BtrpNumber i = new BtrpNumber(0, BtrpNumber.Base.base16);
        BtrpNumber j = i.clone();
        Assert.assertEquals(i, j);

        i = new BtrpNumber(0.5);
        j = i.clone();
        Assert.assertEquals(i, j);

    }
}
