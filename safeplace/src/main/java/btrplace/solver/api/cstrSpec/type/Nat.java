package btrplace.solver.api.cstrSpec.type;

import btrplace.solver.api.cstrSpec.Value;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Nat implements Type {

    private int inf, sup;

    private static final Nat instance = new Nat(0, 5);

    private Nat(int lb, int ub) {
        this.inf = lb;
        this.sup = ub;
    }

    @Override
    public Set domain() {
        Set<Integer> s = new HashSet<>();
        for (int i = inf; i <= sup; i++) {
            s.add(i);
        }
        return s;
    }

    public static Nat getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean isIn(String n) {
        try {
            Integer i = Integer.parseInt(n);
            if (i >= inf && i <= sup) {
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    @Override
    public String label() {
        return "nat";
    }

    @Override
    public Value newValue(String n) {
        //throw new RuntimeException();
        return new Value(Integer.parseInt(n), this);
    }
}
