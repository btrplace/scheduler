package btrplace.instance.json;

import btrplace.instance.DefaultIntResource;
import btrplace.instance.IntResource;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link JSONIntResource}.
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
        String str = s.toJSON(rc);
        IntResource rc2 = s.fromJSON(str);
        System.out.println(s.toJSON(rc));
        System.out.println(str);
        System.out.println(rc2);

    }
}
