package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ConstraintInputGeneratorTest {

    @Test
    public void test() throws Exception {
        SpecReader r = new SpecReader();
        Specification spec = r.getSpecification(new File("src/test/resources/v1.cspec"));

        Constraint cstr = spec.get("spread");
        List<UserVar> args = cstr.getParameters();

    }
}
