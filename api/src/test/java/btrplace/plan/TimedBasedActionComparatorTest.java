package btrplace.plan;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link TimedBasedActionComparator}.
 *
 * @author Fabien Hermenier
 */
public class TimedBasedActionComparatorTest {

    private static TimedBasedActionComparator cmp = new TimedBasedActionComparator();

    @Test
    public void testPrecedence() {
        Action a = new MockAction(UUID.randomUUID(), 0, 4);
        Action b = new MockAction(UUID.randomUUID(), 4, 10);
        Assert.assertTrue(cmp.compare(a, b) < 0);
        Assert.assertTrue(cmp.compare(b, a) > 0);
    }

    @Test
    public void testEquality() {
        Action a = new MockAction(UUID.randomUUID(), 0, 4);
        Action b = new MockAction(UUID.randomUUID(), 0, 4);
        Assert.assertEquals(cmp.compare(a, b), 0);
    }

    @Test
    public void testEqualityWithSimultaneousDisallowed() {
        Action a = new MockAction(UUID.randomUUID(), 0, 4);
        Action b = new MockAction(UUID.randomUUID(), 0, 4);
        Assert.assertNotEquals(new TimedBasedActionComparator(true).compare(a, b), 0);

    }

    @Test
    public void testOverlap1() {
        Action a = new MockAction(UUID.randomUUID(), 0, 4);
        Action b = new MockAction(UUID.randomUUID(), 2, 4);
        Assert.assertTrue(cmp.compare(a, b) < 0);
    }

    @Test
    public void testOverlap2() {
        Action a = new MockAction(UUID.randomUUID(), 0, 4);
        Action b = new MockAction(UUID.randomUUID(), 0, 3);
        Assert.assertTrue(cmp.compare(a, b) > 0);
    }

}
