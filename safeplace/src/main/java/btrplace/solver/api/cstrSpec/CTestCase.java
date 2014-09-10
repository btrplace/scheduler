package btrplace.solver.api.cstrSpec;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.spec.term.Constant;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class CTestCase {

    private String testName;

    private Class testClass;

    private Constraint cstr;

    private List<Constant> args;

    private ReconfigurationPlan plan;

    private boolean continuous;

    private int nb;
    private int number;

    public CTestCase(Class clName, String testName, int nb, Constraint cstr, List<Constant> argv, ReconfigurationPlan p, boolean c) {
        testClass = clName;
        this.testName = testName;
        this.nb = nb;
        this.cstr = cstr;
        args = argv;
        plan = p;
        continuous = c;
    }

    public boolean continuous() {
        return continuous;
    }

    @Override
    public String toString() {
        return "id: " + id() +
                "\nConstraint: " + cstr.toString(args) +
                "\nContinuous: " + continuous() +
                "\nOrigin:\n" + plan.getOrigin().getMapping() +
                "Plan:\n" + plan;
    }

    public Constraint getConstraint() {
        return cstr;
    }

    public List<Constant> getParameters() {
        return args;
    }

    public ReconfigurationPlan getPlan() {
        return plan;
    }

    public String id() {
        return testClass.getSimpleName() + "." + testName + "#" + nb;
    }

    public Class getTestClass() {
        return testClass;
    }

    public String getTestName() {
        return testName;
    }

    public int getNumber() {
        return nb;
    }
}
