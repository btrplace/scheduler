package btrplace.solver.api.cstrSpec.verification.spec;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class IntVerifDomain implements VerifDomain<Integer> {

    private Set<Integer> dom;

    private int lb, ub;

    public IntVerifDomain(int lb, int ub) {
        dom = new HashSet<>(ub - lb + 1);
        for (int i = lb; i <= ub; i++) {
            dom.add(i);
        }
        this.lb = lb;
        this.ub = ub;
    }

    @Override
    public Set<Integer> domain() {
        return dom;
    }

    @Override
    public String type() {
        return "int";
    }

    @Override
    public String toString() {
        return lb + ".." + ub;
    }
}
