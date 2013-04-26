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

    private static TimedBasedActionComparator startCmp = new TimedBasedActionComparator();
    private static TimedBasedActionComparator stopCmp = new TimedBasedActionComparator(false, false);

    @Test
    public void testPrecedence() {
        Action a = new MockAction(UUID.randomUUID(), 0, 4);
        Action b = new MockAction(UUID.randomUUID(), 4, 10);
        Assert.assertTrue(startCmp.compare(a, b) < 0);
        Assert.assertTrue(startCmp.compare(b, a) > 0);

        Assert.assertTrue(stopCmp.compare(a, b) < 0);
        Assert.assertTrue(stopCmp.compare(b, a) > 0);

    }

    @Test
    public void testEquality() {
        Action a = new MockAction(UUID.randomUUID(), 0, 4);
        Action b = new MockAction(UUID.randomUUID(), 0, 4);
        Assert.assertEquals(startCmp.compare(a, b), 0);

        Assert.assertEquals(stopCmp.compare(a, b), 0);
    }

    @Test
    public void testEqualityWithSimultaneousDisallowed() {
        Action a = new MockAction(UUID.randomUUID(), 0, 4);
        Action b = new MockAction(UUID.randomUUID(), 0, 4);
        Assert.assertNotEquals(new TimedBasedActionComparator(true, true).compare(a, b), 0);
        Assert.assertNotEquals(new TimedBasedActionComparator(false, true).compare(a, b), 0);

    }

    @Test
    public void testOverlap1() {
        Action a = new MockAction(UUID.randomUUID(), 0, 4);
        Action b = new MockAction(UUID.randomUUID(), 2, 4);
        Assert.assertTrue(startCmp.compare(a, b) < 0);
        Assert.assertTrue(stopCmp.compare(a, b) < 0);
    }

    @Test
    public void testOverlap2() {
        Action a = new MockAction(UUID.randomUUID(), 0, 4);
        Action b = new MockAction(UUID.randomUUID(), 0, 3);
        Assert.assertTrue(startCmp.compare(a, b) > 0);
        Assert.assertTrue(stopCmp.compare(a, b) > 0);
    }
}
