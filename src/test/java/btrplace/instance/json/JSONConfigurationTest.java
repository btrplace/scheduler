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

package btrplace.instance.json;

import btrplace.instance.Configuration;
import btrplace.instance.DefaultConfiguration;
import junit.framework.Assert;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link JSONConfiguration}.
 *
 * @author Fabien Hermenier
 */
public class JSONConfigurationTest {

    @Test
    public void testTo() {
        Configuration c = new DefaultConfiguration();
        c.addOnlineNode(UUID.randomUUID());
        c.addOfflineNode(UUID.randomUUID());
        c.addOfflineNode(UUID.randomUUID());
        c.addWaitingVM(UUID.randomUUID());
        c.addWaitingVM(UUID.randomUUID());
        c.addWaitingVM(UUID.randomUUID());
        JSONConfiguration json = new JSONConfiguration();
        JSONObject ob = json.toJSON(c);
        Configuration c2 = json.fromJSON(ob.toJSONString());
        Assert.assertEquals(c, c2);
    }
}
