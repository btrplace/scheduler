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

import btrplace.model.*;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link JSONModel}.
 *
 * @author Fabien Hermenier
 */
public class JSONModelTest {

    @Test
    public void testSimple() {
        IntResource rc1 = new DefaultIntResource("foo");
        IntResource rc2 = new DefaultIntResource("bar");
        rc1.set(UUID.randomUUID(), 1);
        rc1.set(UUID.randomUUID(), 2);
        rc1.set(UUID.randomUUID(), 3);

        rc2.set(UUID.randomUUID(), 4);
        rc2.set(UUID.randomUUID(), 5);
        rc2.set(UUID.randomUUID(), 6);

        Mapping cfg = new DefaultMapping();
        cfg.addOnlineNode(UUID.randomUUID());
        cfg.addOnlineNode(UUID.randomUUID());
        cfg.addOfflineNode(UUID.randomUUID());
        cfg.addWaitingVM(UUID.randomUUID());
        Model i = new DefaultModel(cfg);
        i.attach(rc1);
        i.attach(rc2);

        JSONModel j = new JSONModel();
        JSONObject o = j.toJSON(i);
        Model i2 = j.fromJSON(o.toJSONString());
        Assert.assertEquals(i, i2);

    }
}
