package btrplace.solver.api.cstrSpec.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.solver.api.cstrSpec.Term;
import btrplace.solver.api.cstrSpec.type.VMType;

import java.util.Deque;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Hosted implements Function {

    private Term t;

    public Hosted(Deque<Term> stack) {
        this.t = stack.pop();
    }

    //@Override
    public Set domain() {
        return VMType.getInstance().domain();
    }

    //@Override
    public VMType type() {
        return VMType.getInstance();
    }

    @Override
    public String toString() {
        return new StringBuilder("hoster(").append(t).append(")").toString();
    }

    @Override
    public Object getValue(Model mo) {
        Node n = (Node) t.getValue(mo);
        if (n == null) {
            throw new UnsupportedOperationException();
        }
        return mo.getMapping().getRunningVMs(n); //TODO: what about sleepings ?
    }
}
