package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.Term;
import btrplace.solver.api.cstrSpec.invariant.type.IntType;
import btrplace.solver.api.cstrSpec.invariant.type.SetType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Card extends Function {

    private Term<Set> set;

    public static final String ID = "card";

    public Card(Term<Set> stack) {
        this.set = stack;
    }

    @Override
    public Type type() {
        return IntType.getInstance();
    }

    @Override
    public String toString() {
        return new StringBuilder("card(").append(set).append(")").toString();
    }

    @Override
    public Integer eval(Model mo) {
        Collection c = set.eval(mo);
        if (c == null) {
            return null;
        }
        return c.size();
    }

    public static class Builder extends FunctionBuilder {
        @Override
        public Card build(List<Term> args) {
            return new Card(asSet(args.get(0)));
        }

        @Override
        public String id() {
            return Card.ID;
        }

        @Override
        public Type[] signature() {
            return new Type[]{new SetType(null)};
        }
    }
}
