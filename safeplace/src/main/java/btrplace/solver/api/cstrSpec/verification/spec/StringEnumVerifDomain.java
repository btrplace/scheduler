package btrplace.solver.api.cstrSpec.verification.spec;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class StringEnumVerifDomain implements VerifDomain<String> {

    private Set<String> dom;

    private int lb, ub;

    public StringEnumVerifDomain(String[] strs) {
        dom = new HashSet<>(strs.length);
        Collections.addAll(dom, strs);
    }

    @Override
    public Set<String> domain() {
        return dom;
    }

    @Override
    public String type() {
        return "string";
    }

    @Override
    public String toString() {
        return dom.toString();
    }
}
