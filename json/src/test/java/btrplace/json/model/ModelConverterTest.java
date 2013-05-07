package btrplace.json.model;

import btrplace.json.JSONConverterException;
import btrplace.json.model.view.ModelViewsConverter;
import btrplace.model.*;
import btrplace.model.view.ShareableResource;
import btrplace.test.PremadeElements;
import junit.framework.Assert;
import net.minidev.json.JSONObject;
import org.testng.annotations.Test;

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
    public void testConversion() throws JSONConverterException {
        ModelConverter conv = new ModelConverter();
        Mapping m = new DefaultMapping();
        m.addOnlineNode(n1);
        m.addReadyVM(vm1);
        Model mo = new DefaultModel(m);
        Attributes attrs = mo.getAttributes();
        attrs.put(vm1, "boot", 5);
        attrs.put(n1, "type", "xen");

        ShareableResource rc = new ShareableResource("cpu");
        rc.set(vm1, 5);
        rc.set(n1, 10);
        mo.attach(rc);

        JSONObject jo = conv.toJSON(mo);
        Model res = conv.fromJSON(jo);
        Assert.assertEquals(res, mo);
    }
}
