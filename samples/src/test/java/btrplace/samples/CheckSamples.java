package btrplace.samples;

import junit.framework.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests to check the tutorial are working.
 *
 * @author Fabien Hermenier
 */
public class CheckSamples {

    @DataProvider(name = "tutorials")
    public Object[][] getTutorials() {
        return new Object[][]{
                new Object[]{new GettingStarted()},
        };
    }

    @Test(dataProvider = "tutorials")
    public void testBadSignatures(Sample s) throws Exception {
        Assert.assertTrue(s.run());
    }
}
