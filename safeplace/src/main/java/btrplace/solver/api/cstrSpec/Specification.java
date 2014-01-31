package btrplace.solver.api.cstrSpec;

import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Specification {

    private List<Invariant> invs;

    private List<Constraint> cstrs;

    public Specification(List<Invariant> invs, List<Constraint> cstrs) {
        this.invs = invs;
        this.cstrs = cstrs;
    }

    public String pretty() {
        StringBuilder b = new StringBuilder();
        for (Invariant i : invs) {
            b.append(i.pretty()).append('\n');
        }

        for (Constraint c : cstrs) {
            b.append(c.pretty()).append('\n');
        }
        return b.toString();
    }

    public List<Constraint> getConstraints() {
        return cstrs;
    }

    public List<Invariant> getInvariants() {
        return invs;
    }

    public String toString() {
        StringBuilder b = new StringBuilder("Invariant(s): ");

        Iterator ite = invs.iterator();
        while (ite.hasNext()) {
            b.append(ite.next());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }

        b.append("\nConstraint(s): ");
        ite = cstrs.iterator();
        while (ite.hasNext()) {
            b.append(ite.next());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        return b.toString();
    }
}
