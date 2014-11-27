/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.safeplace;

import org.btrplace.safeplace.spec.term.Constant;
import org.btrplace.safeplace.verification.Verifier;

import java.util.Collection;

/**
 * @author Fabien Hermenier
 */
public class CTestCaseMetrology {

    private CTestCase raw;

    private Verifier against;

    private long fuzzingDuration;

    private long testingDuration;

    private long validationDuration;

    private long reduceDuration;

    private long reducedNodes, reducedVMs, reducedArity;

    private CTestCaseResult.Result res;

    public CTestCaseMetrology(CTestCase raw, Verifier against, CTestCaseResult.Result r) {
        this.raw = raw;
        this.against = against;
        res = r;
    }

    public void setFuzzingDuration(long d) {
        this.fuzzingDuration = d;
    }

    public void setTestingDuration(long d) {
        this.testingDuration = d;
    }

    public void setValidationDuration(long d) {
        this.validationDuration = d;
    }

    public void setReduceDuration(long d) {
        this.reduceDuration = d;
    }

    public void setReduced(CTestCase reduced) {
        reducedNodes = nbNodes(reduced);
        reducedVMs = nbVMs(reduced);
        reducedArity = arity(reduced);
    }

    private long nbNodes(CTestCase tc) {
        return tc.getPlan().getOrigin().getMapping().getNbNodes();
    }

    private long nbVMs(CTestCase tc) {
        return tc.getPlan().getOrigin().getMapping().getNbVMs();
    }

    private long nbActions(CTestCase tc) {
        return tc.getPlan().getSize();
    }

    public long getFuzzingDuration() {
        return fuzzingDuration;
    }

    public long getTestingDuration() {
        return testingDuration;
    }

    public long getValidationDuration() {
        return validationDuration;
    }

    public long getReduceDuration() {
        return reduceDuration;
    }

    public long getRawNodes() {
        return nbNodes(raw);
    }

    public long getRawVMs() {
        return nbVMs(raw);
    }

    public long getRawArity() {
        return arity(raw);
    }

    public long getReducedNodes() {
        return reducedNodes;
    }

    public long getReducedVMs() {
        return reducedVMs;
    }

    public long getReducedArity() {
        return reducedArity;
    }

    private long arity(CTestCase tc) {
        long nb = 0;
        for (Constant c : tc.getParameters()) {
            nb += arity(c.eval(null));
        }
        return nb;
    }

    private long arity(Object c) {
        if (c instanceof Collection) {
            long nb = 0;
            for (Object o : (Collection) c) {
                nb += arity(o);
            }
            return nb;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return
                raw.getConstraint().id() + " " + (raw.continuous() ? "continuous" : "discrete") + " " +
                        against.id() + " " + res + " " +
                        fuzzingDuration + " " + validationDuration + " " + testingDuration + " " + reduceDuration +
                        " " + getRawNodes() + " " + getRawVMs() + " " + getRawArity() + " " + reducedNodes + " " + reducedVMs + " " + reducedArity;
    }
}
