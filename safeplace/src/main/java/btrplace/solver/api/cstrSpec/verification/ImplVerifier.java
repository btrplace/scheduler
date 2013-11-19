package btrplace.solver.api.cstrSpec.verification;

import btrplace.model.*;
import btrplace.model.constraint.*;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import choco.kernel.common.logging.ChocoLogging;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class ImplVerifier implements Verifier {


    public TestResult verify(TestCase c) {
        return verify(c, true);
    }

    public TestResult verify(TestCase c, boolean inclSat) {
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getConstraintMapper().register(new CSchedule.Builder());
        Set<SatConstraint> cstrs = new HashSet<>();
        if (inclSat) {
            cstrs.add(c.getSatConstraint());
        }
        cstrs.addAll(actionsToConstraints(c.getPlan(), c.getSatConstraint()));

        setDurationEstimators(c.getPlan());

        //System.out.println(cstrs);
        try {
            cra.labelVariables(true);
            //cra.setVerbosity(3);
            cra.doOptimize(false);
            ReconfigurationPlan p = cra.solve(c.getPlan().getOrigin(), cstrs);
            if (c.isConsistent()) {
                if (p == null) {
                    ChocoLogging.flushLogs();
                    return makeResult(c, TestResult.ErrorType.falseNegative, new Exception("No solution for that problem:\n" + prettyList(cstrs)));
                }
                if (!p.equals(c.getPlan())) {
                    return makeResult(c, TestResult.ErrorType.bug,
                            new Exception("The test case and the solution differ:\n Test Case:\n" + c.getPlan() + "\n Solution:\n" + p));
                }
                return makeResult(c, TestResult.ErrorType.succeed, null);
            } else {
                if (p == null) {
                    return makeResult(c, TestResult.ErrorType.succeed, null);
                }
                if (!p.equals(c.getPlan())) {
                    return makeResult(c, TestResult.ErrorType.bug, new Exception("The test case and the solution differ:\n Test Case:\n" + c.getPlan() + "\n Solution:\n" + p));
                }
                return makeResult(c, TestResult.ErrorType.falsePositive, new Exception("Should not pass"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!c.isConsistent()) {
                return makeResult(c, TestResult.ErrorType.succeed, e);
            }
            return makeResult(c, TestResult.ErrorType.falseNegative, e);
        } catch (Error e) {
            e.printStackTrace();
            if (!c.isConsistent()) {
                return makeResult(c, TestResult.ErrorType.succeed, new Exception(e));
            }
            return makeResult(c, TestResult.ErrorType.falseNegative, new Exception(e));
        }
    }


    private TestResult makeResult(TestCase c, TestResult.ErrorType err, Exception ex) {
        return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), err, ex);
    }

    private static SatConstraint on(VM v, Node n) {
        return new Fence(Collections.singleton(v), Collections.singleton(n));
    }

    private Set<SatConstraint> actionsToConstraints(ReconfigurationPlan p, SatConstraint toTest) {
        Set<Node> notSwitching = new HashSet<>(p.getOrigin().getMapping().getAllNodes());
        Set<SatConstraint> cstrs = new HashSet<>();
        if (toTest instanceof Online || toTest instanceof Offline) {
            //System.out.println("Ignore state unchange for " + toTest);
            notSwitching.removeAll(toTest.getInvolvedNodes());
        }

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


    private List<TestCase> mins = new ArrayList<>();

    public List<TestCase> reduce(TestCase c, Constraint cstr, Map<String, Object> in) {
        reduce(0, c, cstr, in);
        return mins;
    }

    private boolean reduce(int lvl, TestCase t, btrplace.solver.api.cstrSpec.Constraint cstr, Map<String, Object> in) {
        System.out.println(indent(lvl) + "Reduce " + t.getPlan().getActions());
        TestResult res = this.verify(t, false);
        if (res.succeeded()) {
            System.out.println(indent(lvl) + "-> Succeeded. Throw away");
            return true;
        }
        if (t.getPlan().getSize() <= 1) {
            System.out.println(indent(lvl) + "-> Minimal");
            mins.add(t);
            return false;
        } else {
            System.out.println(indent(lvl) + "-> Splittable");
            int middle = t.getPlan().getSize() / 2;
            int sep = middle;
            int max = t.getPlan().getSize();

            boolean decidable = false;
            while (!decidable) {
                ReconfigurationPlan p1 = new DefaultReconfigurationPlan(t.getPlan().getOrigin());
                ReconfigurationPlan p2 = new DefaultReconfigurationPlan(t.getPlan().getOrigin());
                int i = 0;
                for (Action a : t.getPlan()) {
                    if (i++ < sep) {
                        p1.add(a);
                    } else {
                        p2.add(a);
                    }
                }
                System.out.println(indent(lvl) + "split 1: " + p1.getActions());
                System.out.println(indent(lvl) + "split 2: " + p2.getActions());
                TestCase c1 = new TestCase(t.num(), p1, t.getSatConstraint(), cstr.instantiate(in, p1));
                TestCase c2 = new TestCase(t.num(), p2, t.getSatConstraint(), cstr.instantiate(in, p2));
                decidable = reduce(lvl + 1, c1, cstr, in);
                decidable &= reduce(lvl + 1, c2, cstr, in);
                decidable = !decidable;
                sep = (sep + 1) % max;
                if (sep == middle) {
                    System.out.println(indent(lvl) + "unable to make a valuable split");
                    break;
                }
            }
        }
        return false;
    }

    private String indent(int l) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < l; i++) {
            b.append("\t");
        }
        return b.toString();
    }
}
