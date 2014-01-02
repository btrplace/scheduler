package btrplace.solver.api.cstrSpec.invariant;

import btrplace.solver.api.cstrSpec.invariant.func.*;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class Functions {

    private Map<String, FunctionBuilder> funcs;

    public Functions() {
        funcs = new HashMap<>();
    }


    public static Functions newBundle() {
        Functions fc = new Functions();
        fc.declare(new Host.Builder());
        fc.declare(new Hosted.Builder());
        fc.declare(new VMState.Builder());
        fc.declare(new NodeState.Builder());
        fc.declare(new Colocated.Builder());
        fc.declare(new Card.Builder());
        fc.declare(new Cons.Builder());
        fc.declare(new Capa.Builder());
        return fc;
    }

    public void declare(FunctionBuilder fb) {
        funcs.put(fb.id(), fb);
    }

    private void check(FunctionBuilder b, List<Term> args) {
        Type[] expected = b.signature();
        if (expected.length != args.size()) {
            throw new IllegalArgumentException(formatError(b, args));
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(args.get(i).type())) {
                throw new IllegalArgumentException(formatError(b, args));
            }
        }
    }

    private String formatError(FunctionBuilder b, List<Term> args) {
        StringBuilder x = new StringBuilder();
        x.append(b.id()).append(toString(args)).append(" cannot match ").append(toString(b));
        return x.toString();
    }

    private String toString(FunctionBuilder f) {
        StringBuilder b = new StringBuilder();
        b.append(f.id()).append("(");
        Type[] expected = f.signature();
        for (int i = 0; i < expected.length; i++) {
            b.append(expected[i]);
            if (i < expected.length - 1) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

    private String toString(List<Term> args) {
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

    public Function get(String id, List<Term> stack) {
        FunctionBuilder fb = funcs.get(id);
        if (fb == null) {
            throw new RuntimeException("Cannot resolve function '" + id + "'");
        }
        check(fb, stack);
        return fb.build(stack);
/*
        switch (id) {
            case "vmState":
                return new VMState(stack);
            case "nodeState":
                return new NodeState(stack);
            case "colocated":
                return new Colocated(stack);
            case "host":

                return new Host(stack);
            case "card":
                return new Card(stack);
            case "hosted":
                return new Hosted(stack);
            case "cons":
                return new Cons(stack);
            case "capa":
                return new Capa(stack);
            default:
                throw new RuntimeException("Cannot resolve function '" + id + "'");
        }        */
    }
}
