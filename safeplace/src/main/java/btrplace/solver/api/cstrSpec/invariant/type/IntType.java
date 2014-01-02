package btrplace.solver.api.cstrSpec.invariant.type;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.Constant;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class IntType extends Atomic {

    private int inf, sup;

    private static final IntType instance = new IntType(0, 5);

    private IntType(int lb, int ub) {
        this.inf = lb;
        this.sup = ub;
    }

    @Override
    public Set<Integer> domain(Model mo) {
        Set<Integer> s = new HashSet<>();
        for (int i = inf; i <= sup; i++) {
            s.add(i);
        }
        return s;
    }

    public static IntType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean match(String n) {
        try {
            Integer i = Integer.parseInt(n);
            if (i >= inf && i <= sup) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public String label() {
        return "int";
    }

    @Override
    public Constant newValue(String n) {
        return new Constant(Integer.parseInt(n), IntType.getInstance());
    }

    public Constant newValue(int i) {
        return new Constant(i, IntType.getInstance());
    }
}
