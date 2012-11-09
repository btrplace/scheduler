package btrplace.instance.json;

import btrplace.instance.DefaultIntResource;
import btrplace.instance.IntResource;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link JSONIntResource}.
 *
 * @author Fabien Hermenier
 */
public class JSONIntResourceTest {

    @Test
    public void testSimple() {
        IntResource rc = new DefaultIntResource("foo");
        rc.set(UUID.randomUUID(), 3);
        rc.set(UUID.randomUUID(), 4);
        rc.set(UUID.randomUUID(), 5);
        rc.set(UUID.randomUUID(), 6);
        JSONIntResource s = new JSONIntResource();
        String str = s.toJSON(rc).toString();
        IntResource rc2 = s.fromJSON(str);

        Assert.assertEquals(rc.identifier(), rc2.identifier());
        Assert.assertEquals(rc.getDefined(), rc2.getDefined());
        for (UUID u : rc.getDefined()) {
            Assert.assertEquals(rc.get(u), rc2.get(u));
        }
    }
}
