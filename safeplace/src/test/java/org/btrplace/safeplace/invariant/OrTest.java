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

package org.btrplace.safeplace.invariant;

import org.btrplace.safeplace.spec.prop.And;
import org.btrplace.safeplace.spec.prop.Or;
import org.btrplace.safeplace.spec.prop.Proposition;
import org.btrplace.safeplace.verification.spec.SpecModel;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class OrTest {

    @Test
    public void testInstantiation() {
        Or a = new Or(Proposition.False, Proposition.True);
        Assert.assertEquals(a.first(), Proposition.False);
        Assert.assertEquals(a.second(), Proposition.True);
    }

    @DataProvider(name = "input")
    public Object[][] getInputs() {
        return new Object[][]{
                {Proposition.True, Proposition.True, Boolean.TRUE},
                {Proposition.True, Proposition.False, Boolean.TRUE},
                {Proposition.False, Proposition.True, Boolean.TRUE},
                {Proposition.False, Proposition.False, Boolean.FALSE},
        };
    }

    @Test(dataProvider = "input")
    public void testTruthTable(Proposition a, Proposition b, Boolean r) {
        Or p = new Or(a, b);
        Assert.assertEquals(p.eval(new SpecModel()), r);
    }

    @Test
    public void testNot() {
        Or or = new Or(Proposition.True, Proposition.False);
        And o = or.not();
        Assert.assertEquals(o.first(), Proposition.False);
        Assert.assertEquals(o.second(), Proposition.True);
    }
}
