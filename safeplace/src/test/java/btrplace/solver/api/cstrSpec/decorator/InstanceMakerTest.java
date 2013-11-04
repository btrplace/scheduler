package btrplace.solver.api.cstrSpec.decorator;

import btrplace.model.Instance;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link InstanceMaker}.
 *
 * @author Fabien Hermenier
 */
public class InstanceMakerTest {

    @Test
    public void testWoDependencies() {
        InstanceMaker im = InstanceMaker.makeCoreInstancesMaker();
        String [] terms = new String[]{
                "online N1", "offline N2", "running VM1 N1", "sleeping VM2 N1", "ready VM3", "online N3"
        };
        Instance i = im.build(Arrays.asList(terms));
        System.out.println(i.getModel());
    }
}
