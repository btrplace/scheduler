package btrplace.solver.api.cstrSpec.verification.spec;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class FloatVerifDomain implements VerifDomain<Double> {

    private Set<Double> dom;

    private int lb, ub;

    public FloatVerifDomain(int lb, int ub, double inc) {
        dom = new HashSet<>();
        for (double i = lb; i <= ub; i += inc) {
            dom.add(i);
        }
        this.lb = lb;
        this.ub = ub;
    }

    @Override
    public Set<Double> domain() {
        return dom;
    }

    @Override
    public String type() {
        return "float";
    }

    @Override
    public String toString() {
        return lb + ".." + ub;
    }
}
