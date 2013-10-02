package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;

import java.util.Iterator;

/**
 * @author Fabien Hermenier
 */
public class Or extends BinaryProp {

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Iterator<Proposition> ite = iterator(); ite.hasNext(); ) {
            Proposition n = ite.next();
            if (n.size() > 1) {
                b.append('(').append(n).append(')');
            } else {
                b.append(n);
            }
            if (ite.hasNext()) {
                b.append(" | ");
            }
        }
        return b.toString();
    }

    @Override
    public And not() {
        And a = new And();
        for (Proposition p : this) {
            a.add(p.not());
        }
        return a;
    }

    @Override
    public Iterator<Proposition> iterator() {
        return props.iterator();
    }

    public Or add(Proposition p) {
        props.add(p);
        return this;
    }

    @Override
    public boolean inject(Model mo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Or expand() {
        Or or = new Or();
        for (Proposition p : this.props) {
            or.add(p.expand());
        }
        return or;
    }
}
