package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.spec.type.Type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class FunctionCall<T> extends Term<T> {

    private Function<T> c;

    private List<Term> args;

    private boolean current;

    public FunctionCall(Function<T> c, List<Term> args, boolean current) {
        check(c, args);
        this.c = c;
        this.args = args;
        this.current = current;
    }

    @Override
    public Type type() {
        return c.type(args);
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

    private static void check(Function f, List<Term> args) {
        Type[] expected = f.signature(args);
        if (expected.length != args.size()) {
            throw new IllegalArgumentException(toString(f.id(), args) + " cannot match " + f);
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(args.get(i).type())) {
                System.out.println("for " + f.id() + " expected=" + expected[i] + " got=" + args.get(i).type());
                throw new IllegalArgumentException(toString(f.id(), args) + " cannot match " + f);
            }
        }
    }

    public static String toString(String id, List<Term> args) {
        StringBuilder b = new StringBuilder(id);
        b.append('(');
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
