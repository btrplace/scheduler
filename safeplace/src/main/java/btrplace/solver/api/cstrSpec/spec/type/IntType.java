package btrplace.solver.api.cstrSpec.spec.type;

import btrplace.solver.api.cstrSpec.spec.term.Constant;

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

    @Override
    public boolean comparable(Type t) {
        return t.equals(NoneType.getInstance()) || equals(t);
    }
}
