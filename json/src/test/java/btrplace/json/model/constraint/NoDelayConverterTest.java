package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.constraint.NoDelay;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Created by vkherbac on 05/09/14.
 */
public class NoDelayConverterTest {

    @Test
    public void testViables() throws JSONConverterException, IOException {
        Model mo = new DefaultModel();
        NoDelayConverter conv = new NoDelayConverter();
        conv.setModel(mo);
        NoDelay nd = new NoDelay(mo.newVM());
        Assert.assertEquals(conv.fromJSON(conv.toJSONString(nd)), nd);
        System.out.println(conv.toJSONString(nd));
    }
}
