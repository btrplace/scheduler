package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ForAll implements Proposition {

    private List<Variable> vars;

    private Variable from;

    private Proposition prop;

    public ForAll(List<Variable> vars, Variable from, Proposition p) {
        this.vars = vars;
        this.from = from;
        prop = p;
    }

    @Override
    public Proposition not() {
        return new Exists(vars, from, prop.not());
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Boolean evaluate(Model m) {
        boolean ret = true;
        List<List<Object>> values = new ArrayList<>(vars.size());
        for (int i = 0; i < vars.size(); i++) {
            values.add(new ArrayList<>((Collection<Object>)from.getValue(m)));
        }
        for(List<Object> tuple : Collections.allTuples(values)) {
            for (int i = 0; i < tuple.size(); i++) {
                vars.get(i).set(tuple.get(i));
            }
            Boolean r = prop.evaluate(m);
            if (r == null) {
                return null;
            }
            ret &= r;
        }
        for (Variable v : vars) {
            v.unset();
        }
        return ret;
    }

    public String toString() {
        return new StringBuilder("!(").append(enumerate()).append(" : ").append(from.label()).append("). ")
                .append(prop).toString();
    }

    private String enumerate() {
        Iterator<Variable> ite = vars.iterator();
        StringBuilder b = new StringBuilder(ite.next().label());
        while (ite.hasNext()) {
            b.append(",");
            b.append(ite.next().label());
        }
        return b.toString();
    }
}
