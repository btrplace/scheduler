package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;

/**
 * A logical proposition.
 *
 * @author Fabien Hermenier
 */
public interface Proposition {

    Proposition not();

    int size();

    //Proposition expand();

    Boolean evaluate(Model m);

    static final Proposition False = new Proposition() {
        @Override
        public Proposition not() {
            return True;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Boolean evaluate(Model m) {
            return Boolean.FALSE;
        }
    };

    static final Proposition True = new Proposition() {
        @Override
        public Proposition not() {
            return False;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Boolean evaluate(Model m) {
            return Boolean.TRUE;
        }
    };
}
