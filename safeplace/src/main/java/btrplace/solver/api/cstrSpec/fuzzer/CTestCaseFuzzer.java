package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.CTestCase;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class CTestCaseFuzzer implements Iterator<CTestCase>, Iterable<CTestCase> {

    private Constraint cstr;

    private ReconfigurationPlanFuzzer2 rpf;

    private String name;

    private ConstraintInputFuzzer argsf;

    private int nb = 1;

    private List<Constraint> pre;

    public CTestCaseFuzzer(String n, Constraint c, List<Constraint> pre, ReconfigurationPlanFuzzer2 rpf) {
        this.rpf = rpf;
        cstr = c;
        name = n;
        ReconfigurationPlan p = rpf.next();
        SpecModel mo = new SpecModel(p.getOrigin());
        /*for (VerifDomain v : rpf.doms()) {
            mo.add(v);
        } */
        argsf = new ConstraintInputFuzzer(c, mo);
        this.pre = pre;
    }

    @Override
    public Iterator<CTestCase> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public CTestCase next() {
        ReconfigurationPlan p;
        List<Constant> args;
        p = rpf.next();
        args = argsf.newParams();
        //return new CTestCase(name + "_" + (nb++), cstr, args, p);
        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }
}
