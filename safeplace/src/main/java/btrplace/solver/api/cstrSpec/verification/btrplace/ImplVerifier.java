/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.api.cstrSpec.verification.btrplace;

import btrplace.model.*;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.CheckerResult;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import solver.exception.SolverException;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class ImplVerifier implements Verifier {

    private ChocoReconfigurationAlgorithm cra;

    private boolean repair;

    public ImplVerifier() {
        this(false);
    }

    public ImplVerifier(boolean repair) {
        this.repair = repair;
        cra = new DefaultChocoReconfigurationAlgorithm();
        cra.doRepair(repair);
        cra.getConstraintMapper().register(new CSchedule.Builder());
    }

    public boolean repair() {
        return repair;
    }

    public void repair(boolean repair) {
        this.repair = repair;
    }

    private static CheckerResult noSolution = CheckerResult.newFailure("No solution");

    @Override
    public CheckerResult verify(Constraint c, List<Constant> params, Model dst, Model src) {

        List<SatConstraint> cstrs = new ArrayList<>();
        if (!c.isCore()) {
            //TODO: encache the sat constraint
            SatConstraint satC = null;
            try {
                satC = Constraint2BtrPlace.build(c, params);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (!satC.setContinuous(false)) {
                //return null;
                throw new UnsupportedOperationException("Implementation of " + c + " don't support the discrete restriction");
            }
            cstrs.add(satC);

        }
        actionsToConstraints(cstrs, dst);
        try {
            cra.doOptimize(false);
            ReconfigurationPlan res = cra.solve(src, cstrs);
            if (res == null) {
                return noSolution;
            } else {
                return CheckerResult.newSuccess();
            }
        } catch (SolverException ex) {
            return CheckerResult.newFailure(ex.getMessage());
        } catch (Exception e) {
            return CheckerResult.newFailure(e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    @Override
    public CheckerResult verify(Constraint c, List<Constant> params, ReconfigurationPlan p) {
        List<SatConstraint> cstrs = new ArrayList<>();

        if (!c.isCore()) {
            try {
                //TODO: encache the sat constraint
                SatConstraint satC = Constraint2BtrPlace.build(c, params);
                if (!satC.setContinuous(true)) {
                    throw new UnsupportedOperationException("Implementation of " + c + " don't support the continuous restriction");
                }
                cstrs.add(satC);
            } catch (Exception e) {
                if (e instanceof UnsupportedOperationException) {
                    throw (UnsupportedOperationException) e; //yummy
                }
                throw new RuntimeException(e);
            }
        }
        actionsToConstraints(cstrs, p);
        setDurationEstimators(p);
        try {
            cra.doOptimize(false);
            ReconfigurationPlan res = cra.solve(p.getOrigin(), cstrs);
            if (res == null) {
                return noSolution;
            } else if (!p.equals(res)) {
                throw new RuntimeException("The resulting schedule differ. Got:\n" + res + "\nExpected:\n" + p);
            } else {
                return CheckerResult.newSuccess();
            }
        } catch (SolverException ex) {
            /*System.out.flush();
            System.err.println("Possible impl bug with " + c.toString(params));
            System.err.println(p.getOrigin().getMapping());
            System.err.println(p);
            ex.printStackTrace();*/
            return CheckerResult.newFailure(ex.getMessage());
        } catch (Exception e) {
            return CheckerResult.newFailure(e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    private static SatConstraint on(VM v, Node n) {
        return new Fence(v, Collections.singleton(n));
    }

    private void actionsToConstraints(Collection<SatConstraint> cstrs, ReconfigurationPlan p) {
        Set<Node> notSwitching = new HashSet<>(p.getOrigin().getMapping().getOnlineNodes());
        notSwitching.addAll(p.getOrigin().getMapping().getOfflineNodes());
        Set<VM> rooted = new HashSet<>(p.getOrigin().getMapping().getRunningVMs());
        for (Action a : p.getActions()) {
            if (a instanceof MigrateVM) {
                MigrateVM m = (MigrateVM) a;
                cstrs.add(new Running(m.getVM()));
                cstrs.add(on(m.getVM(), m.getDestinationNode()));
                rooted.remove(m.getVM());
            } else if (a instanceof SuspendVM) {
                SuspendVM s = (SuspendVM) a;
                cstrs.add(new Sleeping(s.getVM()));
                cstrs.add(on(s.getVM(), s.getDestinationNode()));
                rooted.remove(s.getVM());
            } else if (a instanceof ResumeVM) {
                ResumeVM s = (ResumeVM) a;
                cstrs.add(new Running(s.getVM()));
                cstrs.add(on(s.getVM(), s.getDestinationNode()));
            } else if (a instanceof BootVM) {
                BootVM s = (BootVM) a;
                cstrs.add(new Running(s.getVM()));
                cstrs.add(on(s.getVM(), s.getDestinationNode()));
            } else if (a instanceof ShutdownVM) {
                ShutdownVM s = (ShutdownVM) a;
                cstrs.add(new Ready(s.getVM()));
            } else if (a instanceof BootNode) {
                BootNode s = (BootNode) a;
                cstrs.add(new Online(s.getNode()));
                notSwitching.remove(s.getNode());
            } else if (a instanceof ShutdownNode) {
                ShutdownNode s = (ShutdownNode) a;
                cstrs.add(new Offline(s.getNode()));
                notSwitching.remove(s.getNode());
            } else if (a instanceof KillVM) {
                cstrs.add(new Killed(((KillVM) a).getVM()));
            } else {
                throw new UnsupportedOperationException(a.toString());
            }

            //Only force the schedule for continuous constraints
            if (a instanceof VMEvent) {
                cstrs.add(new Schedule(((VMEvent) a).getVM(), a.getStart(), a.getEnd()));
            } else if (a instanceof NodeEvent) {
                cstrs.add(new Schedule(((NodeEvent) a).getNode(), a.getStart(), a.getEnd()));
            }
        }
        if (!rooted.isEmpty()) {
            cstrs.addAll(Root.newRoots(rooted));
        }
        Mapping map = p.getOrigin().getMapping();
        for (Node n : notSwitching) {
            if (map.isOnline(n)) {
                cstrs.add(new Online(n));
            } else {
                cstrs.add(new Offline(n));
            }
        }
    }

    private void actionsToConstraints(Collection<SatConstraint> cstrs, Model dst) {
        for (Node n : dst.getMapping().getOnlineNodes()) {
            cstrs.add(new Online(n));
        }

        for (Node n : dst.getMapping().getOfflineNodes()) {
            cstrs.add(new Offline(n));
        }

        for (VM v : dst.getMapping().getRunningVMs()) {
            cstrs.add(new Running(v));
            cstrs.add(on(v, dst.getMapping().getVMLocation(v)));
        }

        for (VM v : dst.getMapping().getSleepingVMs()) {
            cstrs.add(new Sleeping(v));
            cstrs.add(on(v, dst.getMapping().getVMLocation(v)));
        }

        for (VM v : dst.getMapping().getReadyVMs()) {
            cstrs.add(new Ready(v));
        }
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
            } else if (a instanceof KillVM) {
                attrs.put(((KillVM) a).getVM(), "kill", d);
            } else {
                throw new UnsupportedOperationException(a.toString());
            }
        }
    }

    @Override
    public String toString() {
        if (repair) {
            return "impl_repair";
        }
        return "impl";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImplVerifier that = (ImplVerifier) o;

        return repair == that.repair;
    }

    @Override
    public int hashCode() {
        return (repair ? 1 : 0);
    }
}
