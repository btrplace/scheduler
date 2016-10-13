package org.btrplace.safeplace.spec.term.func;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface Function<T> {

    Type type();

    String id();

    T eval(Context mo, Object... args);

    Type[] signature();

    default Type type(List<Term> args) { return type();}

    static String toString(Function f) {
        return Arrays.stream(f.signature()).map(Type::toString).collect(Collectors.joining(",",f.id() + "(",")"));
    }
}
