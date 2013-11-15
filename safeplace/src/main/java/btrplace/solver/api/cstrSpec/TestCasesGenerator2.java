package btrplace.solver.api.cstrSpec;

import btrplace.json.model.constraint.ConstraintsConverter;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.generator.Generator;
import btrplace.solver.api.cstrSpec.generator.ReconfigurationPlansGenerator;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class TestCasesGenerator2 implements Generator<TestCase> {

    private Constraint cstr;

    private ReconfigurationPlansGenerator pg;

    private ConstraintInputGenerator cg;

    private ConstraintsConverter cstrC;

    private int max;

    private int k = 0;

    public TestCasesGenerator2(Constraint c, ReconfigurationPlansGenerator pg, boolean seq, int max) {
        this.max = max;
        this.cstr = c;
        this.pg = pg;
        this.cg = new ConstraintInputGenerator(c, seq);

        cstrC = ConstraintsConverter.newBundle();
    }

    @Override
    public TestCase next() {
        k++;
        try {
            ReconfigurationPlan p = pg.next();
            cstrC.setModel(p.getOrigin());
            Map<String, Object> vals = cg.next();
            SatConstraint satCstr = (SatConstraint) cstrC.fromJSON(JSONs.marshal(cstr.getMarshal(), vals));
            Boolean gr = cstr.instantiate(vals, p);
            return new TestCase(p, satCstr, gr);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public int count() {
        return max;
    }

    @Override
    public int done() {
        return k;
    }

    @Override
    public void reset() {
        k = 0;
    }

    @Override
    public Iterator<TestCase> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return k < count();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}