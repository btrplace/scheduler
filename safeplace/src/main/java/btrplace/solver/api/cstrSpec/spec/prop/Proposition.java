package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

/**
 * A logical proposition.
 *
 * @author Fabien Hermenier
 */
public interface Proposition {

    Proposition not();

    Boolean eval(SpecModel m);

    static final Proposition False = new Proposition() {
        @Override
        public Proposition not() {
            return True;
        }

        @Override
        public Boolean eval(SpecModel m) {
            return Boolean.FALSE;
        }

        @Override
        public String toString() {
            return "false";
        }

        @Override
        public Proposition simplify(SpecModel m) {
            return this;
        }
    };

    static final Proposition True = new Proposition() {
        @Override
        public Proposition not() {
            return False;
        }

        @Override
        public Boolean eval(SpecModel m) {
            return Boolean.TRUE;
        }

        @Override
        public String toString() {
            return "true";
        }

        @Override
        public Proposition simplify(SpecModel m) {
            return this;
        }
    };

    Proposition simplify(SpecModel m);
}
