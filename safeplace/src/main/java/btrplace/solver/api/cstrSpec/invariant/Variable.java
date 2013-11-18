package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

/**
 * @author Fabien Hermenier
 */
public class Variable implements Term {

    private Type t;

    private String n;

    private Object val = null;

    public Variable(String n, Type t) {
        this.t = t;
        this.n = n;
    }

    //@Override
    /*public Set<Object> domain() {
        if (val == null) {
            return t.domain();
        }
        return Collections.singleton(val);
    } */

    @Override
    public String toString() {
        if (val != null) {
            return val.toString();
        }
        return label();
    }

    public Type type() {
        return t;
    }

    public String label() {
        //if (val == null) {
        return n;
        /*}
        return n + "="+val;*/
    }

    public boolean set(Object v) {
        val = v;
        return true;
    }

    public void unset() {
        val = null;
    }

    @Override
    public Object getValue(Model mo) {
        return val;
    }
}
