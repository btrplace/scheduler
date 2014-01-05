package btrplace.solver.api.cstrSpec.spec.type;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.term.Constant;

import java.util.HashSet;
import java.util.Set;

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

    @Override
    public Set<Double> domain(Model mo) {
        Set<Double> s = new HashSet<>();
        //TODO: Beurk
        for (double i = inf; i <= sup; i += 0.1) {
            s.add(i);
        }
        return s;
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
