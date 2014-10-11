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
 * Unit tests for {@link BtrpSet}
 *
 * @author Fabien Hermenier
 */
@Test
public class BtrpSetTest {

    public void testInstantiation() {
        BtrpSet s = new BtrpSet(4, BtrpOperand.Type.node);
        Assert.assertEquals(s.degree(), 4);
        Assert.assertEquals(s.type(), BtrpOperand.Type.node);
        Assert.assertEquals(s.getValues().size(), 0);
        Assert.assertEquals(s.prettyType(), "set<set<set<set<node>>>>");
    }

    public void testToString() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.getValues().add(new BtrpNumber(5, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(7, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(9, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(10, BtrpNumber.Base.base10));
        Assert.assertNotNull(s.toString());
    }

    public void testViablePower() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.getValues().add(new BtrpNumber(2, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(3, BtrpNumber.Base.base10));
        BtrpSet res = s.power(new BtrpNumber(2, BtrpNumber.Base.base10));
        Assert.assertEquals(res.degree(), 2);
        Assert.assertEquals(res.type(), BtrpOperand.Type.number);
        Assert.assertEquals(res.size(), 2);
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViablePower() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.getValues().add(new BtrpNumber(2, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(3, BtrpNumber.Base.base10));
        Model mo = new DefaultModel();
        s.power(new BtrpElement(BtrpOperand.Type.VM, "V", mo.newVM()));
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViablePower2() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.getValues().add(new BtrpNumber(2, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(3, BtrpNumber.Base.base10));
        s.power(new BtrpNumber(-2, BtrpNumber.Base.base10));
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViablePower3() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.getValues().add(new BtrpNumber(2, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(3, BtrpNumber.Base.base10));
        s.power(new BtrpNumber(5.2));
    }

    public void testViableAddition() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.getValues().add(new BtrpNumber(2, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(3, BtrpNumber.Base.base10));

        BtrpSet s2 = new BtrpSet(1, BtrpOperand.Type.number);
        s.getValues().add(new BtrpNumber(5, BtrpNumber.Base.base10));

        BtrpSet res = s.plus(s2);
        Assert.assertEquals(res.type(), BtrpOperand.Type.number);
        Assert.assertEquals(res.degree(), 1);
        Assert.assertEquals(res.getValues().size(), 3);
        Assert.assertTrue(res.getValues().containsAll(s.getValues()));
        Assert.assertTrue(res.getValues().containsAll(s2.getValues()));

        //Addition with duplicates, must merge
        s2.getValues().add(new BtrpNumber(3, BtrpNumber.Base.base10));
        res = s.plus(s2);
        Assert.assertEquals(res.getValues().size(), 3);
        Assert.assertTrue(res.getValues().containsAll(s.getValues()));
        Assert.assertTrue(res.getValues().containsAll(s2.getValues()));
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableAdditionCauseDegree() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.getValues().add(new BtrpNumber(2, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(3, BtrpNumber.Base.base10));

        BtrpSet s2 = new BtrpSet(2, BtrpOperand.Type.number);
        s.plus(s2);
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableAdditionCauseType() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.getValues().add(new BtrpNumber(2, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(3, BtrpNumber.Base.base10));

        BtrpSet s2 = new BtrpSet(1, BtrpOperand.Type.VM);
        s.plus(s2);
    }


    public void testViableDifference() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.getValues().add(new BtrpNumber(1, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(2, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(3, BtrpNumber.Base.base10));

        BtrpSet s2 = new BtrpSet(1, BtrpOperand.Type.number);
        s2.getValues().add(new BtrpNumber(3, BtrpNumber.Base.base10));
        s2.getValues().add(new BtrpNumber(5, BtrpNumber.Base.base10));

        BtrpSet res = s.minus(s2);
        Assert.assertEquals(res.type(), BtrpOperand.Type.number);
        Assert.assertEquals(res.degree(), 1);
        Assert.assertEquals(res.getValues().size(), 2);
        for (BtrpOperand o : s2.getValues()) {
            Assert.assertFalse(res.getValues().contains(o));
        }
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableDifferenceCauseDegree() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.getValues().add(new BtrpNumber(2, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(3, BtrpNumber.Base.base10));

        BtrpSet s2 = new BtrpSet(2, BtrpOperand.Type.number);
        s.minus(s2);
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableDifferenceCauseType() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.getValues().add(new BtrpNumber(2, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(3, BtrpNumber.Base.base10));

        BtrpSet s2 = new BtrpSet(1, BtrpOperand.Type.VM);
        s.minus(s2);
    }

    public void testViableTimes() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.getValues().add(new BtrpNumber(2, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(3, BtrpNumber.Base.base10));

        BtrpSet s2 = new BtrpSet(1, BtrpOperand.Type.number);
        s2.getValues().add(new BtrpNumber(1, BtrpNumber.Base.base10));
        s2.getValues().add(new BtrpNumber(3, BtrpNumber.Base.base10));
        BtrpSet res = s.times(s2);
        //In theory: {{2,1},{2,3},{3,2},{3,3}, {3,1}}. But {3,3} not allowed and {2,3} == {3,2}
        Assert.assertEquals(res.degree(), 2);
        Assert.assertEquals(res.type(), BtrpOperand.Type.number);
        System.out.println(res.toString());
        Assert.assertEquals(res.size(), 3);

        BtrpSet s3 = new BtrpSet(1, BtrpOperand.Type.number);
        res = s.times(s3);
        Assert.assertEquals(res, s);
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableTimesCauseType() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.getValues().add(new BtrpNumber(2, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(3, BtrpNumber.Base.base10));

        BtrpSet s2 = new BtrpSet(1, BtrpOperand.Type.VM);
        s.times(s2);
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableTimesCauseDegree() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.getValues().add(new BtrpNumber(2, BtrpNumber.Base.base10));
        s.getValues().add(new BtrpNumber(3, BtrpNumber.Base.base10));

        BtrpSet s2 = new BtrpSet(2, BtrpOperand.Type.VM);
        s.times(s2);
    }

    public void testViableDiv() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        for (int i = 0; i < 11; i++) {
            s.getValues().add(new BtrpNumber(i, BtrpNumber.Base.base10));
        }

        BtrpSet res = s.div(new BtrpNumber(3, BtrpNumber.Base.base10));
        Assert.assertEquals(res.degree(), 2);
        Assert.assertEquals(res.size(), 3); // 3 subsets (4 elmts, 4 elmts, 3 elmts)
        Assert.assertEquals(((BtrpSet) res.getValues().get(0)).size(), 4);
        Assert.assertEquals(((BtrpSet) res.getValues().get(1)).size(), 4);
        Assert.assertEquals(((BtrpSet) res.getValues().get(2)).size(), 3);
    }

    /**
     * 100 elements divided in 4 partitions, each will be composed of 25 elements.
     * additional test to fix a bug undetected before v0.101
     */
    public void testViableDiv2() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        for (int i = 0; i < 100; i++) {
            s.getValues().add(new BtrpNumber(i, BtrpNumber.Base.base10));
        }

        BtrpSet res = s.div(new BtrpNumber(4, BtrpNumber.Base.base10));
        Assert.assertEquals(res.degree(), 2);
        Assert.assertEquals(res.size(), 4); // 3 subsets (4 elmts, 4 elmts, 3 elmts)
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(((BtrpSet) res.getValues().get(i)).size(), 25);
        }
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableDivCauseType() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        Model mo = new DefaultModel();
        s.div(new BtrpElement(BtrpOperand.Type.VM, "V", mo.newVM()));
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableDivCauseType2() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.div(new BtrpNumber(3.2));
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableDivCauseZero() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);

        s.div(new BtrpNumber(0, BtrpNumber.Base.base10));
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableDivCauseToBig() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        for (int i = 0; i < 6; i++) {
            s.getValues().add(new BtrpNumber(i, BtrpNumber.Base.base10));
        }
        s.div(new BtrpNumber(7, BtrpNumber.Base.base10));
    }


    public void testViableRemainder() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        for (int i = 0; i < 6; i++) {
            s.getValues().add(new BtrpNumber(i, BtrpNumber.Base.base10));
        }

        BtrpSet res = s.remainder(new BtrpNumber(2, BtrpNumber.Base.base10));
        //System.err.println(res);
        Assert.assertEquals(res.degree(), 2);
        Assert.assertEquals(res.size(), 3); // 3 subsets of 2 elements each
        for (BtrpOperand o : res.getValues()) {
            Assert.assertEquals(((BtrpSet) o).size(), 2);
        }

    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableRemainderCauseType() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        Model mo = new DefaultModel();
        s.remainder(new BtrpElement(BtrpOperand.Type.VM, "V", mo.newVM()));
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableRemainderCauseType2() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.remainder(new BtrpNumber(4.3));
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testNonViableRemainderCauseZero() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        s.remainder(new BtrpNumber(0, BtrpNumber.Base.base10));
    }

    public void testEq() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        BtrpSet s2 = new BtrpSet(1, BtrpOperand.Type.number);
        for (int i = 0; i < 6; i++) {
            s.getValues().add(new BtrpNumber(i, BtrpNumber.Base.base10));
            s2.getValues().add(new BtrpNumber(i, BtrpNumber.Base.base10));
        }

        Assert.assertEquals(s.eq(s2), BtrpNumber.TRUE);
        Assert.assertEquals(s, s2);

        BtrpSet s3 = new BtrpSet(1, BtrpOperand.Type.number);
        Assert.assertEquals(s.eq(s3), BtrpNumber.FALSE);

        BtrpSet s4 = new BtrpSet(2, BtrpOperand.Type.number);
        Assert.assertEquals(s.eq(s4), BtrpNumber.FALSE);

        BtrpSet s5 = new BtrpSet(2, BtrpOperand.Type.VM);
        Assert.assertEquals(s.eq(s5), BtrpNumber.FALSE);

    }

    public void testClone() {
        BtrpSet s = new BtrpSet(1, BtrpOperand.Type.number);
        for (int i = 0; i < 6; i++) {
            s.getValues().add(new BtrpNumber(i, BtrpNumber.Base.base10));
        }
        BtrpSet s2 = s.clone();
        Assert.assertEquals(s, s2);
        s.getValues().add(new BtrpNumber(12, BtrpNumber.Base.base10));
        Assert.assertNotEquals(s, s2);
    }
}
