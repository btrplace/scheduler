package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;

/**
 * Logical and between several propositions.
 * @author Fabien Hermenier
 */
public class And extends BinaryProp {

    public And(Proposition p1, Proposition p2) {
        super(p1, p2);
    }

    @Override
    public String operator() {
        return " & ";
    }

    @Override
    public Or not() {
        return new Or(p1.not(), p2.not());
    }

/*    public Or develop() {
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
    }       */

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

        Boolean r1 = p1.evaluate(m);
        if (r1 == null) {
            return null;
        }
        Boolean r2 = p2.evaluate(m);
        if (r2 == null) {
            return null;
        }
        return r1 && r2;
    }
}