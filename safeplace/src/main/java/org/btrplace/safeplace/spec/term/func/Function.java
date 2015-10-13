package org.btrplace.safeplace.spec.term.func;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.verification.spec.Context;

import java.util.List;

/**
 * Created by fhermeni on 14/09/2015.
 */
public interface Function<T> {

    Type type();

    String id();

    T eval(Context mo, Object... args);

    Type[] signature();

    Type type(List<Term> args);
}
