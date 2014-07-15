package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Fabien Hermenier
 */
public class ConstraintInputFuzzer {


    private Random rnd;

    private List<Constant>[] domains;

    public ConstraintInputFuzzer(Constraint cstr, SpecModel mo) {
        domains = new ArrayList[cstr.getParameters().size()];
        rnd = new Random();

        //cache the domains
        int i = 0;
        for (UserVar v : cstr.getParameters()) {
            domains[i++] = v.domain(mo);
        }
    }

    public List<Constant> newParams() {
        List<Constant> l = new ArrayList<>(domains.length);
        for (List<Constant> dom : domains) {
            l.add(dom.get(rnd.nextInt(dom.size())));
        }
        return l;
    }

}
