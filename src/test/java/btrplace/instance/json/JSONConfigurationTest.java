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
