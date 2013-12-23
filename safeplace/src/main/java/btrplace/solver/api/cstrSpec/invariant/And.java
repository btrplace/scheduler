package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;

import java.util.Iterator;

/**
 * Logical and between several propositions.
 * @author Fabien Hermenier
 */
public class And extends NaryProp {

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Iterator<Proposition> ite = this.iterator(); ite.hasNext(); ) {
            Proposition n = ite.next();
            if (n.size() > 1) {
                b.append('(').append(n).append(')');
            } else {
                b.append(n);
            }
            if (ite.hasNext()) {
                b.append(" & ");
            }
        }
        return b.toString();
    }

    @Override
    public Or not() {
        Or o = new Or();
        for (Proposition p : this) {
            o.add(p.not());
        }
        return o;
    }

    public And add(Proposition p) {
        props.add(p);
        return this;
    }

    public Or develop() {
        Or l = new Or();
        int[] indexes = new int[size()];
        int i = 0;
        int nbStates = 1;
        for (Proposition p : this) {
            indexes[i++] = 0;
            nbStates *= p.size();
        }

        for (int k = 0; k < nbStates; k++) {
            And and = new And();
            for (int x = 0; x < size(); x++) {
                and.add(((Or) get(x)).get(indexes[x]));
            }
            l.add(and);
            for (int x = 0; x < size(); x++) {
                indexes[x]++;
                if (indexes[x] < get(x).size()) {
                    break;
                }
                indexes[x] = 0;
            }
        }
        return l;
    }

    /*@Override
    public And expand() {
        And and = new And();
        for (Proposition p : this.props) {
            and.add(p.expand());
        }
        return and;
    }        */

    @Override
    public Boolean evaluate(Model m) {
        boolean ret = true;
        for (Proposition p : this) {
            Boolean r = p.evaluate(m);
            if (r == null) {
                return null;
            }
            ret &= r;
        }
        return ret;
    }
}