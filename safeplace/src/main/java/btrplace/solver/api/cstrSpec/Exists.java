package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.type.Type;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Exists implements Proposition {

    private Variable v;

    private Proposition prop;

    public Exists(Variable iterator, Proposition p) {
        this.v = iterator;
        prop = p;
    }

    public Variable getVariable(){
        return v;
    }

    //@Override
    public Type type() {
        throw new UnsupportedOperationException();
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
    public boolean inject(Model mo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean evaluate(Model m) {
        boolean ret = false;
        Set<Object> dom = v.domain();
            for (Object val : dom) {
            v.set(val);
            Boolean r = prop.evaluate(m);
            if (r == null) {
                return null;
            }
            ret |= r;
        }
        v.unset();
        return ret;
    }

    public void associate(Proposition p) {
        this.prop = p;
    }

    public String toString() {
        return "!(" + v.label() + " : " + v.type() + "). " + prop.toString();
    }
}
