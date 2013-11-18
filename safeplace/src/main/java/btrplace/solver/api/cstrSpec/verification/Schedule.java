package btrplace.solver.api.cstrSpec.verification;

import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.checker.SatConstraintChecker;

import java.util.Collections;

/**
 * @author Fabien Hermenier
 */
public class Schedule extends SatConstraint {

    private int start, end;

    private VM vm;

    private Node n;

    public Schedule(VM v, int st, int ed) {
        super(Collections.singleton(v), Collections.<Node>emptyList(), true);
        this.start = st;
        this.end = ed;
        vm = v;
    }

    public Schedule(Node n, int st, int ed) {
        super(Collections.<VM>emptyList(), Collections.singleton(n), true);
        this.start = st;
        this.end = ed;
        this.n = n;
    }


    public VM getVM() {
        return vm;
    }

    public Node getNode() {
        return n;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "schedule(" + (vm == null ? n : vm) + ", " + start + "," + end + ")";
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new ScheduleChecker(this);
    }
}
