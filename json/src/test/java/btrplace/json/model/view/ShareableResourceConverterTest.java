/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

package btrplace.json.model.view;

import btrplace.json.JSONConverterException;
import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.view.ShareableResource;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.io.IOException;


/**
 * Unit tests for {@link btrplace.json.model.view.ShareableResourceConverter}.
 *
 * @author Fabien Hermenier
 */
public class ShareableResourceConverterTest {

    @Test
    public void testSimple() throws JSONConverterException, IOException {
        Model mo = new DefaultModel();
        ShareableResource rc = new ShareableResource("foo", 3, 8);
        rc.setConsumption(mo.newVM(), 3);
        rc.setConsumption(mo.newVM(), 4);
        rc.setCapacity(mo.newNode(), 5);
        rc.setCapacity(mo.newNode(), 6);
        ShareableResourceConverter s = new ShareableResourceConverter();
        s.setModel(mo);
        ShareableResource rc2 = s.fromJSON(s.toJSONString(rc));
        Assert.assertEquals(rc, rc2);
/*        Assert.assertEquals(rc.getIdentifier(), rc2.getIdentifier());
        Assert.assertEquals(rc.getResourceIdentifier(), rc2.getResourceIdentifier());
        Assert.assertEquals(rc.getDefinedVMs(), rc2.getDefinedVMs());
        Assert.assertEquals(rc.getDefinedNodes(), rc2.getDefinedNodes());
        for (VM u : rc.getDefinedVMs()) {
            Assert.assertEquals(rc.getConsumption(u), rc2.getConsumption(u));
        }
        for (Node u : rc.getDefinedNodes()) {
            Assert.assertEquals(rc.getCapacity(u), rc2.getCapacity(u));
        }
                                 */
    }

    @Test(dependsOnMethods = {"testSimple"})
    public void testWithDifferentRcId() throws JSONConverterException, IOException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        ShareableResourceConverter s = new ShareableResourceConverter();
        s.setModel(mo);
        ShareableResource rc = new ShareableResource("foo");
        rc.setConsumption(vm1, 3).setConsumption(vm2, 4).setCapacity(n1, 5);
        ShareableResource rcBis = s.fromJSON(s.toJSONString(rc));

        ShareableResource rc2 = new ShareableResource("bar");
        rc2.setConsumption(vm1, 3).setConsumption(vm2, 4).setCapacity(n1, 5);

        ShareableResource rc2Bis = s.fromJSON(s.toJSONString(rc2));
        Assert.assertFalse(rcBis.equals(rc2Bis));

    }
}
