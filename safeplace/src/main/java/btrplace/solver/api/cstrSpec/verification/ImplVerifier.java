package btrplace.solver.api.cstrSpec.verification;

import btrplace.model.*;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import choco.kernel.common.logging.ChocoLogging;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class ImplVerifier implements Verifier {

    @Override
    public TestResult verify(TestCase c) {
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getConstraintMapper().register(new CSchedule.Builder());
        List<SatConstraint> cstrs = actionsToConstraints(c.getPlan());
        setDurationEstimators(c.getPlan());
        cstrs.add(c.getSatConstraint());
        try {
            //cra.setVerbosity(3);
            cra.doOptimize(false);
            ReconfigurationPlan p = cra.solve(c.getPlan().getOrigin(), cstrs);
            if (c.isConsistent()) {
                if (p == null) {
                    ChocoLogging.flushLogs();
                    return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), TestResult.ErrorType.falseNegative, new Exception("No solution for that problem:\n" + prettyList(cstrs)));
                }
                if (!p.equals(c.getPlan())) {
                    return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), TestResult.ErrorType.bug, new Exception("The test case and the solution differ:\n Test Case:\n" + c.getPlan() + "\n Solution:\n" + p));
                }
                return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), TestResult.ErrorType.succeed);
            } else {
                if (p == null) {
                    return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), TestResult.ErrorType.succeed);
                }
                if (!p.equals(c.getPlan())) {
                    return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), TestResult.ErrorType.bug, new Exception("The test case and the solution differ:\n Test Case:\n" + c.getPlan() + "\n Solution:\n" + p));
                }
                return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), TestResult.ErrorType.falsePositive, new Exception("WTF:\n" + p));
            }
        } catch (Exception e) {
            //e.printStackTrace();
            if (!c.isConsistent()) {
                return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), TestResult.ErrorType.succeed, e);
            }
            return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), TestResult.ErrorType.falseNegative, e);
        } catch (Error e) {
            e.printStackTrace();
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

            if (a instanceof VMEvent) {
                cstrs.add(new Schedule(((VMEvent) a).getVM(), a.getStart(), a.getEnd()));
            } else if (a instanceof NodeEvent) {
                cstrs.add(new Schedule(((NodeEvent) a).getNode(), a.getStart(), a.getEnd()));
            }

        }
        if (!rooted.isEmpty()) {
            cstrs.add(new Root(rooted));
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

    private void setDurationEstimators(ReconfigurationPlan rp) {
        Model mo = rp.getOrigin();
        Attributes attrs = mo.getAttributes();
        for (Action a : rp) {
            int d = a.getEnd() - a.getStart();
            if (a instanceof MigrateVM) {
                attrs.put(((MigrateVM) a).getVM(), "migrate", d);
            } else if (a instanceof BootVM) {
                attrs.put(((BootVM) a).getVM(), "boot", d);
            } else if (a instanceof ShutdownVM) {
                attrs.put(((ShutdownVM) a).getVM(), "shutdown", d);
            } else if (a instanceof SuspendVM) {
                attrs.put(((SuspendVM) a).getVM(), "suspend", d);
            } else if (a instanceof ResumeVM) {
                attrs.put(((ResumeVM) a).getVM(), "resume", d);
            } else if (a instanceof BootNode) {
                attrs.put(((BootNode) a).getNode(), "boot", d);
            } else if (a instanceof ShutdownNode) {
                attrs.put(((ShutdownNode) a).getNode(), "shutdown", d);
            } else {
                throw new UnsupportedOperationException(a.toString());
            }
        }
    }

    private String prettyList(Collection c) {
        StringBuilder b = new StringBuilder();
        for (Object o : c) {
            b.append("\t").append(o).append("\n");
        }
        return b.toString();
    }

}
