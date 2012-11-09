package btrplace.instance.json;

import btrplace.instance.Configuration;
import btrplace.instance.DefaultConfiguration;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: fhermeni
 * Date: 08/11/12
 * Time: 22:50
 * To change this template use File | Settings | File Templates.
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
        System.out.println(json.toJSON(c));
    }
}
