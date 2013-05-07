package btrplace;

import btrplace.examples.Example;
import btrplace.examples.GettingStarted;
import junit.framework.Assert;
import org.testng.annotations.Test;

/**
 * Launch the examples and check for their termination.
 *
 * @author Fabien Hermenier
 */
public class TestExamples {

    @Test(timeOut = 2000)
    public void testGettingStarted() throws Exception {
        Example ex = new GettingStarted();
        Assert.assertTrue("Example " + ex.toString() + " failed", ex.run());
    }
}
