package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.generator.TuplesGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
        List<List<Object>> values = new ArrayList<>(vars.size());
        for (int i = 0; i < vars.size(); i++) {
            values.add(new ArrayList<>((Collection<Object>)from.getValue(m)));
        }
        TuplesGenerator<Object> tg = new TuplesGenerator<>(Object.class, values);
        for(Object [] tuple : tg) {
            for (int i = 0; i < tuple.length; i++) {
                vars.get(i).set(tuple[i]);
            }
            Boolean r = prop.evaluate(m);
            if (r == null) {
                return null;
            }
            ret |= r;
        }
        for (Variable v : vars) {
            v.unset();
        }
        return ret;
    }

    public void associate(Proposition p) {
        this.prop = p;
    }

    public String toString() {
        return new StringBuilder("#(").append(enumerate()).append(" : ").append(from.label()).append("). ")
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
    }}
