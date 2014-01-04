package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.func.Function2;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class FunctionCall<T> extends Term<T> {

    private Function2<T> c;

    private List<Term> args;

    private boolean current;

    public FunctionCall(Function2<T> c, List<Term> args, boolean current) {
        check(c, args);
        this.c = c;
        this.args = args;
        this.current = current;
    }

    @Override
    public Type type() {
        return c.type();
    }

    @Override
    public T eval(Model m) {
        List<Object> values = new ArrayList<>();
        for (Term t : args) {
            values.add(t.eval(m));
        }
        return c.eval(m, values);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (current) {
            b.append('$');
        }
        b.append(c.id()).append('(');
        Iterator<Term> ite = args.iterator();
        while (ite.hasNext()) {
            b.append(ite.next().toString());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

    private static void check(Function2 f, List<Term> args) {
        Type[] expected = f.signature();
        if (expected.length != args.size()) {
            throw new IllegalArgumentException(formatError(f, args));
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(args.get(i).type())) {
                throw new IllegalArgumentException(formatError(f, args));
            }
        }
    }

    private static String formatError(Function2 b, List<Term> args) {
        StringBuilder x = new StringBuilder(b.id());
        x.append(toString(args)).append(" cannot match ").append(b);
        return x.toString();
    }

    private static String toString(List<Term> args) {
        StringBuilder b = new StringBuilder();
        b.append("(");
        Iterator<Term> ite = args.iterator();
        while (ite.hasNext()) {
            b.append(ite.next().type());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

}
