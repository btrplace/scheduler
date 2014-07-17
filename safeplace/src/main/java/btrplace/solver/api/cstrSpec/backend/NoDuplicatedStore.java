package btrplace.solver.api.cstrSpec.backend;

import btrplace.solver.api.cstrSpec.verification.TestCase;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Fabien Hermenier
 */
public class NoDuplicatedStore implements Countable {

    private BlockingQueue<TestCase> defiant;
    private BlockingQueue<TestCase> compliant;

    public NoDuplicatedStore() {
        defiant = new LinkedBlockingQueue<>();
        compliant = new LinkedBlockingQueue<>();
    }

    @Override
    public void addDefiant(TestCase c) {
        defiant.add(c);
    }

    @Override
    public void addCompliant(TestCase c) {
        compliant.add(c);
    }

    public Set<TestCase> getDefiant() {
        Set<TestCase> s = new HashSet<>();
        for (TestCase tc : defiant) {
            s.add(tc);
        }
        return s;
    }

    public Set<TestCase> getCompliant() {
        Set<TestCase> s = new HashSet<>();
        for (TestCase tc : compliant) {
            s.add(tc);
        }
        return s;
    }

    @Override
    public int getNbCompliant() {
        return getCompliant().size();
    }

    @Override
    public int getNbDefiant() {
        return getDefiant().size();
    }

    @Override
    public void flush() {
    }
}
