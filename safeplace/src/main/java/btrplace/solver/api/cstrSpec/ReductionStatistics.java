package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.spec.term.Constant;

import java.util.Collection;

/**
 * @author Fabien Hermenier
 */
public class ReductionStatistics {

    public ReductionStatistics() {

    }

    public void report(CTestCase tc1, CTestCase tc2) {
        //System.out.print(nbNodes(tc1) + " " + nbVMs(tc1) + " " + nbActions(tc1) + " " + arity(tc1));
        //System.out.println(" || " + nbNodes(tc2) + " " + nbVMs(tc2) + " " + nbActions(tc2) + " " + arity(tc2));
    }

    private int nbNodes(CTestCase tc) {
        return tc.getPlan().getOrigin().getMapping().getNbNodes();
    }

    private int nbVMs(CTestCase tc) {
        return tc.getPlan().getOrigin().getMapping().getNbVMs();
    }

    private int nbActions(CTestCase tc) {
        return tc.getPlan().getSize();
    }

    private int arity(CTestCase tc) {
        int nb = 0;
        for (Constant c : tc.getParameters()) {
            nb += arity(c.eval(null));
        }
        return nb;
    }

    private int arity(Object c) {
        if (c instanceof Collection) {
            int nb = 0;
            for (Object o : (Collection) c) {
                nb += arity(o);
            }
            return nb;
        } else {
            return 1;
        }
    }
}
