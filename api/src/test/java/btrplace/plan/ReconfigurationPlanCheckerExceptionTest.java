package btrplace.plan;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import junit.framework.Assert;
import org.mockito.Mockito;
import org.testng.annotations.Test;

/**
 * Basic unit tests for {@link ReconfigurationPlanCheckerException}.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanCheckerExceptionTest {

    @Test
    public void testInstantiationWithModel() {
        SatConstraint c = Mockito.mock(SatConstraint.class);
        Model m = new DefaultModel(new DefaultMapping());
        ReconfigurationPlanCheckerException ex = new ReconfigurationPlanCheckerException(c, m, true);
        Assert.assertEquals(ex.getModel(), m);
        Assert.assertEquals(ex.isOrigin(), true);
        Assert.assertNull(ex.getAction());

        ex = new ReconfigurationPlanCheckerException(c, m, false);
        Assert.assertEquals(ex.isOrigin(), false);
    }

    @Test
    public void testInstantiationWithAction() {
        SatConstraint c = Mockito.mock(SatConstraint.class);
        Action a = Mockito.mock(Action.class);
        ReconfigurationPlanCheckerException ex = new ReconfigurationPlanCheckerException(c, a);
        Assert.assertEquals(ex.getAction(), a);
        Assert.assertNull(ex.getModel());
    }
}
