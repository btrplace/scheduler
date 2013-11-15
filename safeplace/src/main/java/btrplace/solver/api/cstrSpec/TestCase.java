package btrplace.solver.api.cstrSpec;

import btrplace.model.Mapping;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.ReconfigurationPlanChecker;
import btrplace.plan.ReconfigurationPlanCheckerException;
import btrplace.plan.event.*;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class TestCase {

    private ReconfigurationPlan plan;

    private SatConstraint cstr;

    private boolean isConsistent;

    public TestCase(ReconfigurationPlan p, SatConstraint cstr, boolean c) {
        this.plan = p;
        this.cstr = cstr;
        this.isConsistent = c;
    }

/*    public static TestCase fromJSON(JSONObject o) throws JSONConverterException {
        ConstraintsConverter cv = ConstraintsConverter.newBundle();
        ModelConverter mc = new ModelConverter();
        Model mo = mc.fromJSON((JSONObject) o.get("model"));
        cv.setModel(mo);
        SatConstraint cstr = (SatConstraint) cv.fromJSON((JSONObject) o.get("cstr"));
        boolean c = Boolean.parseBoolean(o.get("isConsistent").toString());
        return new TestCase(mo, cstr, c);
    }    */

    public boolean isConsistent() {
        return this.isConsistent;
    }

    public ReconfigurationPlan getPlan() {
        return plan;
    }

    public SatConstraint getSatConstraint() {
        return this.cstr;
    }

    /*public JSONObject toJSON() throws JSONConverterException {
        JSONObject jo = new JSONObject();
        ConstraintsConverter cv = ConstraintsConverter.newBundle();
        ModelConverter mc = new ModelConverter();
        cv.setModel(mo);
        jo.put("cstr", cv.toJSON(cstr));
        jo.put("model", mc.toJSON(mo));
        jo.put("consistent", isConsistent);
        return jo;
    }     */

    public TestResult verify() {
        TestResult.ErrorType rc = TestResult.ErrorType.bug, ri = TestResult.ErrorType.bug;
        try {
            rc = verifyChecker();
            ri = verifyImpl();
            return new TestResult(plan, cstr, isConsistent, rc, ri);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new TestResult(plan, cstr, isConsistent, rc, ri, ex.getMessage());
        } catch (Error err) {
            err.printStackTrace();
            return new TestResult(plan, cstr, isConsistent, rc, ri, err.getMessage());
        }
    }

    public TestResult.ErrorType verifyChecker() {
        ReconfigurationPlanChecker c = new ReconfigurationPlanChecker();
        c.addChecker(cstr.getChecker());

        try {
            c.check(plan);
        } catch (ReconfigurationPlanCheckerException ex) {
            ex.printStackTrace();
            return isConsistent ? TestResult.ErrorType.falseNegative : TestResult.ErrorType.falsePositive;
        }
        return isConsistent ? TestResult.ErrorType.succeed : TestResult.ErrorType.falsePositive;
    }

    public TestResult.ErrorType verifyImpl() throws SolverException {
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();

        List<SatConstraint> cstrs = actionsToConstraints(plan);

        cra.doOptimize(true);
        ReconfigurationPlan p = cra.solve(plan.getOrigin(), cstrs);
        if (isConsistent) {
            if (p != null && p.getSize() == 0) {
                return TestResult.ErrorType.succeed;
            } else if (p == null) {
                return TestResult.ErrorType.falseNegative;
            }
            return TestResult.ErrorType.falseNegative;
        } else {
            if (p == null) {
                return TestResult.ErrorType.succeed;
            } else if (p.getSize() == 0) {
                return TestResult.ErrorType.falsePositive;
            }
            return TestResult.ErrorType.succeed;
        }
    }

    private static SatConstraint on(VM v, Node n) {
        return new Fence(Collections.singleton(v), Collections.singleton(n));
    }

    private List<SatConstraint> actionsToConstraints(ReconfigurationPlan p) {
        List<SatConstraint> cstrs = new ArrayList<>();
        Set<Node> notSwitching = new HashSet<>(p.getOrigin().getMapping().getAllNodes());
        for (Action a : p) {
            if (a instanceof MigrateVM) {
                MigrateVM m = (MigrateVM) a;
                cstrs.add(new Running(Collections.singleton(m.getVM())));
                cstrs.add(on(m.getVM(), m.getDestinationNode()));
                //TODO: force the node to be online or let that be infered by BtrPlace ?
            } else if (a instanceof SuspendVM) {
                SuspendVM s = (SuspendVM) a;
                cstrs.add(new Sleeping(Collections.singleton(s.getVM())));
                cstrs.add(on(s.getVM(), s.getDestinationNode()));
            } else if (a instanceof ResumeVM) {
                ResumeVM s = (ResumeVM) a;
                cstrs.add(new Sleeping(Collections.singleton(s.getVM())));
                cstrs.add(on(s.getVM(), s.getDestinationNode()));
            } else if (a instanceof BootVM) {
                BootVM s = (BootVM) a;
                cstrs.add(new Running(Collections.singleton(s.getVM())));
                cstrs.add(on(s.getVM(), s.getDestinationNode()));
            } else if (a instanceof ShutdownVM) {
                ShutdownVM s = (ShutdownVM) a;
                cstrs.add(new Ready(Collections.singleton(s.getVM())));
            } else if (a instanceof BootNode) {
                BootNode s = (BootNode) a;
                cstrs.add(new Online(Collections.singleton(s.getNode())));
                notSwitching.remove(s.getNode());
            } else if (a instanceof ShutdownNode) {
                ShutdownNode s = (ShutdownNode) a;
                cstrs.add(new Offline(Collections.singleton(s.getNode())));
                notSwitching.remove(s.getNode());
            } else {
                throw new UnsupportedOperationException(a.toString());
            }

        }

        Mapping map = p.getOrigin().getMapping();
        for (Node n : notSwitching) {
            if (map.isOnline(n)) {
                cstrs.add(new Online(Collections.singleton(n)));
            } else {
                cstrs.add(new Offline(Collections.singleton(n)));
            }
        }
        return cstrs;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("cstr: ").append(cstr).append("\n");
        b.append("plan:\n").append(plan).append("\n");
        b.append("consistent:").append(isConsistent);
        return b.toString();
    }
}

