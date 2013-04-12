package btrplace.plan.event;

import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link SubstitutedVMEvent}.
 *
 * @author Fabien Hermenier
 */
public class SubstitutedVMEventTest implements PremadeElements {

    static SubstitutedVMEvent s = new SubstitutedVMEvent(vm1, vm2);

    @Test
    public void testInstantiation() {
        Assert.assertEquals(s.getVM(), vm1);
        Assert.assertEquals(s.getNewUUID(), vm2);
        Assert.assertFalse(s.toString().contains("null"));
    }

    @Test
    public void testVisit() {
        ActionVisitor visitor = mock(ActionVisitor.class);
        s.visit(visitor);
        verify(visitor).visit(s);
    }
}
