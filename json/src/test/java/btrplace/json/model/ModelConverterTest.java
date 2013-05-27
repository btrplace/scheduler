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

package btrplace.json.model;

import btrplace.json.JSONConverterException;
import btrplace.json.model.view.ModelViewsConverter;
import btrplace.model.Attributes;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.view.ShareableResource;
import btrplace.test.PremadeElements;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Unit tests for {@link ModelConverterTest}.
 *
 * @author Fabien Hermenier
 */
public class ModelConverterTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        ModelConverter conv = new ModelConverter();
        Assert.assertNotNull(conv.getViewsConverter());
        ModelViewsConverter vc = new ModelViewsConverter();
        conv.setModelViewConverters(vc);
        Assert.assertEquals(conv.getViewsConverter(), vc);
    }

    @Test
    public void testConversion() throws JSONConverterException, IOException {
        ModelConverter conv = new ModelConverter();
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        m.addOnlineNode(n1);
        m.addReadyVM(vm1);
        Attributes attrs = mo.getAttributes();
        attrs.put(vm1, "boot", 5);
        attrs.put(n1, "type", "xen");

        ShareableResource rc = new ShareableResource("cpu");
        rc.set(vm1, 5);
        rc.set(n1, 10);
        mo.attach(rc);

        String jo = conv.toJSONString(mo);
        Model res = conv.fromJSON(jo);
        Assert.assertEquals(res, mo);
    }
}
