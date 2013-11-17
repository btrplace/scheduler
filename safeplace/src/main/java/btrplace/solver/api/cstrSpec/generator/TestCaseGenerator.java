package btrplace.solver.api.cstrSpec.generator;

import btrplace.json.model.constraint.ConstraintsConverter;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.ConstraintInputGenerator;
import btrplace.solver.api.cstrSpec.verification.TestCase;

import java.util.Iterator;

/**
 * @author Fabien Hermenier
 */
public class TestCaseGenerator implements Generator<TestCase> {

    private boolean verbose;

    private Constraint c;
    private int nbScheds;
    private ModelsGenerator mg;

    private ConstraintsConverter cconv;

    private ConstraintInputGenerator cg;

    public TestCaseGenerator(ConstraintsConverter cconv, Constraint c, ModelsGenerator mg) {
        this(cconv, c, mg, 1);
    }

    public TestCaseGenerator(ConstraintsConverter cconv, Constraint c, ModelsGenerator mg, int nbSchedules) {
        this.c = c;
        this.nbScheds = nbSchedules;
        this.mg = mg;
        this.cconv = cconv;

        verbose = false;
        cg = new ConstraintInputGenerator(c, true);
    }

    @Override
    public int count() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int done() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<TestCase> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasNext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestCase next() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void verbose(boolean b) {
        this.verbose = b;
    }

    public boolean verbose() {
        return this.verbose;
    }
}
