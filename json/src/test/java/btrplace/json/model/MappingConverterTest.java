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

package btrplace.json.model;

import btrplace.json.JSONConverterException;
import btrplace.model.DefaultMapping;
import btrplace.model.Mapping;
import junit.framework.Assert;
import net.minidev.json.JSONObject;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link btrplace.json.model.MappingConverter}.
 *
 * @author Fabien Hermenier
 */
public class MappingConverterTest {

    @Test
    public void testSimple() throws JSONConverterException {
        Mapping c = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID n3 = UUID.randomUUID();
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        UUID vm4 = UUID.randomUUID();

        c.addOnlineNode(n1);
        c.addOfflineNode(n2);
        c.addRunningVM(vm1, n1);
        c.addSleepingVM(vm2, n1);
        c.addReadyVM(vm3);
        c.addOnlineNode(n3);
        c.addRunningVM(vm4, n3);
        MappingConverter json = new MappingConverter();
        JSONObject ob = json.toJSON(c);
        Mapping c2 = json.fromJSON(ob);
        Assert.assertEquals(c, c2);
    }
}
