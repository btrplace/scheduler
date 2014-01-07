package btrplace.solver.api.cstrSpec.spec.type;

import btrplace.solver.api.cstrSpec.spec.term.Constant;

/**
 * @author Fabien Hermenier
 */
public class RealType extends Atomic {

    private double inf, sup;

    private static final RealType instance = new RealType(0, 5);

    private RealType(int lb, int ub) {
        this.inf = lb;
        this.sup = ub;
    }

    public static RealType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean match(String n) {
        try {
            Double i = Double.parseDouble(n);
            if (i >= inf && i <= sup) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public String label() {
        return "real";
    }

    @Override
    public Constant newValue(String n) {
        return new Constant(Double.parseDouble(n), RealType.getInstance());
    }

    public Constant newValue(double i) {
        return new Constant(i, RealType.getInstance());
    }
}
