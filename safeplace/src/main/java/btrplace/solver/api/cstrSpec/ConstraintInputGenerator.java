package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.generator.AllTuplesGenerator;
import btrplace.solver.api.cstrSpec.generator.Generator;
import btrplace.solver.api.cstrSpec.generator.RandomTuplesGenerator;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class ConstraintInputGenerator implements Generator<Map<String, Object>> {

    private Constraint cstr;

    private String[] ids;
    private Generator<Object[]> tg;

    public ConstraintInputGenerator(Constraint cstr, boolean seq) {
        this.cstr = cstr;

        List<Variable> params = cstr.getParameters();
        List<List<Object>> values = new ArrayList<>(params.size());
        ids = new String[params.size()];
        for (int i = 0; i < params.size(); i++) {
            Variable v = params.get(i);
            ids[i] = v.label();
            values.add(new ArrayList<>(v.domain()));
        }

        if (seq) {
            tg = new AllTuplesGenerator<>(Object.class, values);
        } else {
            tg = new RandomTuplesGenerator<>(Object.class, values);
        }
    }

    @Override
    public boolean hasNext() {
        return tg.hasNext();
    }

    @Override
    public Map<String, Object> next() {
        Object[] tuple = tg.next();
        Map<String, Object> m = new HashMap<>(tuple.length);
        for (int i = 0; i < tuple.length; i++) {
            m.put(ids[i], tuple[i]);
        }
        return m;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Map<String, Object>> iterator() {
        return this;
    }

    @Override
    public int count() {
        return tg.count();
    }

    @Override
    public int done() {
        return tg.done();
    }

    @Override
    public void reset() {
        tg.reset();
    }
}
