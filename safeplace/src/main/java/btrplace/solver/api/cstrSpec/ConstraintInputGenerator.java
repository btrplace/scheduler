package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.fuzzer.RandomTuplesGenerator;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import btrplace.solver.api.cstrSpec.util.AllTuplesGenerator;
import btrplace.solver.api.cstrSpec.util.Generator;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class ConstraintInputGenerator implements Generator<List<Object>> {


    private String[] ids;
    private Generator<Object[]> tg;

    public ConstraintInputGenerator(Constraint cstr, SpecModel mo, boolean seq) {

        List<UserVar> params = cstr.getParameters();
        List<List<Object>> values = new ArrayList<>(params.size());
        ids = new String[params.size()];
        for (int i = 0; i < params.size(); i++) {
            UserVar v = params.get(i);
            Object o = v.eval(null);
            if (o != null) {
                values.add(Collections.singletonList(o));
            } else {
                values.add(new ArrayList<Object>((Set) v.getBackend().eval(mo)));
            }
            ids[i] = v.label();
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
    public List<Object> next() {
        Object[] tuple = tg.next();
        List<Object> m = new ArrayList<>(tuple.length);
        Collections.addAll(m, tuple);
        return m;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<List<Object>> iterator() {
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
