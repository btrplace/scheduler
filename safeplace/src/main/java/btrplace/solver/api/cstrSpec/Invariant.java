package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.prop.Proposition;
import btrplace.solver.api.cstrSpec.spec.term.Primitive;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Invariant {

    private Proposition p;

    private Proposition not;

    private List<Primitive> primitives;

    private String invName;

    public Invariant(String n, Proposition p, List<Primitive> primitives) {
        this.p = p;
        this.not = p.not();
        this.invName = n;
        this.primitives = primitives;
    }

    public Proposition getProposition() {
        return p;
    }

    public Boolean eval(Model res) {

        if (res == null) {     //TODO: flaw ?
            return false;
        }
        Boolean bOk = this.p.eval(res);
        Boolean bKO = this.not.eval(res);

        if (bOk == null || bKO == null) {
            throw new RuntimeException("Both null !\ngood:" + this.p + "\nnotGood: " + not + "\n" + res.getMapping().toString());
        }
        if (bOk && bKO) {
            throw new RuntimeException("Good and bad !\ngood:" + this.p + "\nnotGood: " + not + "\n" + res.getMapping().toString());
        } else if (!(bOk || bKO)) {
            throw new RuntimeException("Nor good or bad !\ngood:" + this.p + "\nnotGood: " + not + "\n" + res.getMapping().toString());
        }
        return bOk;
    }

    public String id() {
        return invName;
    }

    public String pretty() {
        StringBuilder b = new StringBuilder(toString()).append(" ::=\n");
        b.append('\t').append(p);
        return b.toString();
    }

    @Override
    public String toString() {
        return id();
    }
}
