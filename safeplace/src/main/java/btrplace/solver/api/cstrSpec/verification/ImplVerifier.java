package btrplace.solver.api.cstrSpec.verification;

import btrplace.model.Mapping;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class ImplVerifier implements Verifier {

    @Override
    public TestResult verify(TestCase c) {
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();

        List<SatConstraint> cstrs = actionsToConstraints(c.getPlan());

        cstrs.add(c.getSatConstraint());
        try {
            cra.doOptimize(true);
            ReconfigurationPlan p = cra.solve(c.getPlan().getOrigin(), cstrs);
            if (c.isConsistent()) {
                if (p == null) {
                    return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), TestResult.ErrorType.falseNegative);
                }
                return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), TestResult.ErrorType.succeed);
            } else {
                if (p == null) {
                    return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), TestResult.ErrorType.succeed);
                }
                return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), TestResult.ErrorType.falsePositive, new Exception("WTF:\n" + p));
            }
        } catch (Exception e) {
            if (!c.isConsistent()) {
                return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), TestResult.ErrorType.succeed, e);
            }
            return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), TestResult.ErrorType.falseNegative, e);
        } catch (Error e) {
            if (!c.isConsistent()) {
                return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), TestResult.ErrorType.succeed, new Exception(e));
            }
            return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), TestResult.ErrorType.falseNegative, new Exception(e));
        }
    }

    private static SatConstraint on(VM v, Node n) {
        return new Fence(Collections.singleton(v), Collections.singleton(n));
    }

    private List<SatConstraint> actionsToConstraints(ReconfigurationPlan p) {
        List<SatConstraint> cstrs = new ArrayList<>();
        Set<Node> notSwitching = new HashSet<>(p.getOrigin().getMapping().getAllNodes());
        Set<VM> rooted = new HashSet<>(p.getOrigin().getMapping().getRunningVMs());
        for (Action a : p) {
            if (a instanceof MigrateVM) {
                MigrateVM m = (MigrateVM) a;
                cstrs.add(new Running(Collections.singleton(m.getVM())));
                cstrs.add(on(m.getVM(), m.getDestinationNode()));
                rooted.remove(m.getVM());
            } else if (a instanceof SuspendVM) {
                SuspendVM s = (SuspendVM) a;
                cstrs.add(new Sleeping(Collections.singleton(s.getVM())));
                cstrs.add(on(s.getVM(), s.getDestinationNode()));
                rooted.remove(s.getVM());
            } else if (a instanceof ResumeVM) {
                ResumeVM s = (ResumeVM) a;
                cstrs.add(new Running(Collections.singleton(s.getVM())));
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
        cstrs.add(new Root(rooted));
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
}
