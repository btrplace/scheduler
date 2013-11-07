package btrplace.solver.api.cstrSpec.func;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.Term;
import btrplace.solver.api.cstrSpec.type.NatType;
import btrplace.solver.api.cstrSpec.type.Type;

import java.util.Collection;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Card extends Function {

    private Term set;
    public Card(List<Term> stack) {
        this.set = stack.get(0);
    }

    /*@Override
    public Set<Value> domain() {
        Set<Value> d = set.domain();
        Set<Value> dom = new HashSet();
        for (Value v : d) {
            dom.add(new Value(v.domain().size(), NatType.getInstance()));
        }
        return dom;
    } */

    //@Override
    public Type type() {
        return NatType.getInstance();
    }

    @Override
    public String toString() {
        return new StringBuilder("card(").append(set).append(")").toString();
    }

    @Override
    public Integer getValue(Model mo) {
        Collection c = (Collection) set.getValue(mo);
        if (c == null) {
            return null;
        }
        return c.size();
    }
}
