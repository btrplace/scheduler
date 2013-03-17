package btrplace;

import btrplace.examples.Example;
import btrplace.examples.GettingStarted;
import junit.framework.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class TestExamples {

    @DataProvider(name = "examples")
    public Object[][] getExamples() {
        return new Example[][]{
                new Example[]{new GettingStarted()},
        };
    }

    @Test(dataProvider = "examples")
    public void testExamples(Example ex) throws Exception {
            Assert.assertTrue("Example " + ex.toString() + " failed", ex.run());
    }
}
