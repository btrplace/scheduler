package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;

/**
 * @author Fabien Hermenier
 */
public interface Term {


/*    Set<Value> domain();

    Type type();*/

    Object getValue(Model mo);

    Term plus(Term t2);

    Term minus(Term t2);

    Term mult(Term t2);

    Term div(Term t2);

    Term inter(Term t2);

    Term union(Term t2);
}
