package btrplace.solver.api.cstrSpec.verification;

/**
 * @author Fabien Hermenier
 */
public class VerifiedTestCase {

    private boolean res;

    private TestCase tc;

    public VerifiedTestCase(TestCase tc, boolean res) {
        this.res = res;
        this.tc = tc;
    }

    public boolean succeeded() {
        return res;
    }

    public TestCase testCase() {
        return tc;
    }
}
