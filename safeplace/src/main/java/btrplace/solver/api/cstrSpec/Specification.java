package btrplace.solver.api.cstrSpec;

import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Specification {

    private List<Constraint> cstrs;

    public Specification(List<Constraint> cstrs) {
        this.cstrs = cstrs;
    }

    public String pretty() {
        StringBuilder b = new StringBuilder();
        for (Constraint c : cstrs) {
            b.append(c.pretty()).append('\n');
        }
        return b.toString();
    }

    public List<Constraint> getConstraints() {
        return cstrs;
    }

    public Constraint get(String id) {
        for (Constraint c : cstrs) {
            if (c.id().equals(id)) {
                return c;
            }
        }
        return null;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        Iterator<Constraint> ite = cstrs.iterator();
        while (ite.hasNext()) {
            b.append(ite.next());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        return b.toString();
    }
}
