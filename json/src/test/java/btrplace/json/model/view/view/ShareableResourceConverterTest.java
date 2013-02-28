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

package btrplace.json.model.view.view;

import btrplace.json.model.view.ShareableResourceConverter;
import btrplace.model.view.ShareableResource;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link btrplace.json.model.view.ShareableResourceConverter}.
 *
 * @author Fabien Hermenier
 */
public class ShareableResourceConverterTest {

    @Test
    public void testSimple() {
        ShareableResource rc = new ShareableResource("foo");
        rc.set(UUID.randomUUID(), 3);
        rc.set(UUID.randomUUID(), 4);
        rc.set(UUID.randomUUID(), 5);
        rc.set(UUID.randomUUID(), 6);
        ShareableResourceConverter s = new ShareableResourceConverter();
        ShareableResource rc2 = s.fromJSON(s.toJSON(rc));

        Assert.assertEquals(rc.getIdentifier(), rc2.getIdentifier());
        Assert.assertEquals(rc.getResourceIdentifier(), rc2.getResourceIdentifier());
        Assert.assertEquals(rc.getDefined(), rc2.getDefined());
        for (UUID u : rc.getDefined()) {
            Assert.assertEquals(rc.get(u), rc2.get(u));
        }
    }
}
