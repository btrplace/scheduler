package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;

/**
 * A logical proposition.
 *
 * @author Fabien Hermenier
 */
public interface Proposition {

    Proposition not();

    Boolean eval(Model m);

    static final Proposition False = new Proposition() {
        @Override
        public Proposition not() {
            return True;
        }

        @Override
        public Boolean eval(Model m) {
            return Boolean.FALSE;
        }

        @Override
        public String toString() {
            return "false";
        }
    };

    static final Proposition True = new Proposition() {
        @Override
        public Proposition not() {
            return False;
        }

        @Override
        public Boolean eval(Model m) {
            return Boolean.TRUE;
        }

        @Override
        public String toString() {
            return "true";
        }

    };
}
