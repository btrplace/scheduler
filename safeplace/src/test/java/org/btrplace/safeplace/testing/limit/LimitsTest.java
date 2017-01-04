/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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

package org.btrplace.safeplace.testing.limit;

import org.btrplace.safeplace.testing.Result;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class LimitsTest {

    @Test
    public void testSimple() {
        Limits l = new Limits();
        l.failures(1);
        Assert.assertFalse(l.test(Result.CRASH));

        //Override
        l.failures(3);
        Assert.assertTrue(l.test(Result.CRASH));

        l.tests(1);
        Assert.assertFalse(l.test(Result.SUCCESS));//because of maxtests

        l.clear();
        Assert.assertTrue(l.test(Result.CRASH));
    }
}