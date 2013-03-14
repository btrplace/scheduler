package btrplace.plan;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Simple unit tests for {@link Dependency}.
 *
 * @author Fabien Hermenier
 */
public class DependencyTest {

    @Test
    public void testInstantiation() {
        Action a = new MockAction(UUID.randomUUID(), 1, 4);
        Set<Action> d = new HashSet<Action>();
        d.add(new MockAction(UUID.randomUUID(), 2, 5));
        d.add(new MockAction(UUID.randomUUID(), 3, 7));
        Dependency dep = new Dependency(a, d);
        Assert.assertEquals(dep.getAction(), a);
        Assert.assertEquals(dep.getDependencies(), d);
        Assert.assertFalse(dep.toString().contains("null"));
    }
}
