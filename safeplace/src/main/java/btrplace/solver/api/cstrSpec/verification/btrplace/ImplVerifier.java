package btrplace.solver.api.cstrSpec.verification.btrplace;

import btrplace.model.*;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;
import btrplace.solver.SolverException;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.CheckerResult;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class ImplVerifier implements Verifier {

    private boolean repair;

    public ImplVerifier() {
        this(false);
    }

    public ImplVerifier(boolean repair) {
        this.repair = repair;
    }

    public boolean isRepair() {
        return repair;
    }

    public void setRepair(boolean repair) {
        this.repair = repair;
    }

    @Override
    public CheckerResult verify(Constraint c, ReconfigurationPlan p, List<Constant> params, boolean discrete) {
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.doRepair(repair);
        cra.getConstraintMapper().register(new CSchedule.Builder());
        Set<SatConstraint> cstrs = new HashSet<>();

        SatConstraint sat = null;
        try {
            sat = Constraint2BtrPlace.build(c, params);
        } catch (Exception e) {
            return CheckerResult.newError(e.getMessage());
        }
        //FIXME: if involved in the plan
        cstrs.add(sat);
        System.out.println("Initial: " + cstrs + " with \n" + p);
        cstrs.addAll(actionsToConstraints(p, sat));
        setDurationEstimators(p);

        System.out.println("plan2cstrs:\n" + cstrs);

        //Test if the asked constraints belong to the plan

        try {
            cra.labelVariables(true);
            //cra.setVerbosity(3);
            cra.doOptimize(false);
            ReconfigurationPlan res = cra.solve(p.getOrigin(), cstrs);
            if (res == null) {
                return CheckerResult.newFailure();
            } else if (!p.equals(res)) {
                return CheckerResult.newError("The resulting schedule differ. Got:\n" + res + "\nExpected:\n" + p);
            } else {
                return CheckerResult.newSucess();
            }
        } catch (SolverException ex) {
            return CheckerResult.newFailure(ex.getMessage());
        }
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
}
