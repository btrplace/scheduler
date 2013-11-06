package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;

import java.util.Collection;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Exists implements Proposition {

    private List<Variable> vars;

    private Proposition prop;

    private Variable from;
    public Exists(List<Variable> iterator, Variable from, Proposition p) {
        this.vars = iterator;
        prop = p;
        this.from = from;
    }

    @Override
    public Proposition not() {
        throw new UnsupportedOperationException();
    }


    @Override
    public int size() {
        return 1;
    }

    @Override
    public Boolean evaluate(Model m) {
        boolean ret = true;
        for (Object val : (Collection) from.getValue(m)) {
            vars.get(0).set(val);
            Boolean r = prop.evaluate(m);
            if (r == null) {
                return null;
            }
            ret |= r;
        }
        vars.get(0).unset();
        return ret;
    }

    public void associate(Proposition p) {
        this.prop = p;
    }

    public String toString() {
        /*return "!(" + v + " : " + v.type() + "). " + prop.toString();*/
        return "";
    }
}
