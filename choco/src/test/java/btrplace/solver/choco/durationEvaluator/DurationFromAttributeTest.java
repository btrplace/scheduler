/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.durationEvaluator;

import btrplace.model.Attributes;
import btrplace.model.DefaultAttributes;
import btrplace.solver.choco.DurationEvaluator;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link DurationFromAttribute}.
 *
 * @author Fabien Hermenier
 */
public class DurationFromAttributeTest implements PremadeElements {

    @Test
    public void test() {
        Attributes attrs = new DefaultAttributes();


        DurationEvaluator parent = new ConstantDuration(15);
        DurationFromAttribute dev = new DurationFromAttribute(attrs, "boot", parent);
        Assert.assertEquals(parent, dev.getParent());
        Assert.assertEquals("boot", dev.getAttributeKey());
        Assert.assertEquals(15, dev.evaluate(vm1));

        attrs.put(vm1, "boot", 7);
        Assert.assertEquals(7, dev.evaluate(vm1));

        parent = new ConstantDuration(2);
        dev.setParent(parent);
        attrs.clear();
        Assert.assertEquals(2, dev.evaluate(vm1));
        Assert.assertEquals(parent, dev.getParent());
        Assert.assertFalse(dev.toString().contains("null"));

    }
}
