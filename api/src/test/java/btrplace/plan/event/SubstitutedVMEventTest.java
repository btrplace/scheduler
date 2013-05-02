package btrplace.plan.event;

import btrplace.model.*;
import btrplace.model.view.ModelView;
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

    @Test
    public void testApply() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addReadyVM(vm1);
        map.addReadyVM(vm3);
        ModelView v = mock(ModelView.class);
        Model mo = new DefaultModel(map);
        mo.attach(v);
        Assert.assertTrue(s.apply(mo));
        verify(v).substitute(vm1, vm2);
    }
}
