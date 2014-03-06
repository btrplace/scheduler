package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.spec.SpecReader;

import java.io.File;

/**
 * @author Fabien Hermenier
 */
public class SpecStatistics {

    public static void main(String[] args) throws Exception {
        SpecReader r = new SpecReader();
        Specification spec = r.getSpecification(new File(args[0]));
        System.out.println("id length");
        for (Constraint c : spec.getConstraints()) {
            int l = c.pretty().length();
            System.out.println(c.pretty());
            System.out.println(c.id() + " " + l);
        }
    }
}
