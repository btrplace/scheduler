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

package org.btrplace.safeplace.verification;

import org.btrplace.model.Model;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.safeplace.Constraint;
import org.btrplace.safeplace.spec.term.Constant;

import java.util.List;
import java.util.Objects;

/**
 * @author Fabien Hermenier
 */
public class TestCase {

    private Constraint c;

    private ReconfigurationPlan plan;

    private List<Constant> args;

    private CheckerResult expected, got;

    private Verifier verifier;

    private boolean d;

    public TestCase(CheckerResult specRes,
                    CheckerResult againstRes,
                    Verifier v,
                    Constraint c,
                    ReconfigurationPlan p,
                    List<Constant> args,
                    boolean d) {
        expected = specRes;
        got = againstRes;
        verifier = v;
        this.args = args;
        this.c = c;
        this.plan = p;
        this.d = d;
    }

    public TestCase(Verifier v, Constraint c, ReconfigurationPlan p, List<Constant> args, boolean d) {
        /*if (d) {
            expected = new SpecVerifier().verify(c, src, dst, args);
            got = v.verify(c, src, dst, args);
        } else {
            expected = new SpecVerifier().verify(c, p, args);
            got = v.verify(c, p, args);
        }    */

        verifier = v;
        this.args = args;
        this.c = c;
        this.plan = p;
        this.d = d;
    }

    public boolean succeed() {
        return expected.getStatus() == got.getStatus();
    }

    public Verifier getVerifier() {
        return verifier;
    }

    public Constraint getConstraint() {
        return c;
    }

    public ReconfigurationPlan getPlan() {
        return plan;
    }

    public List<Constant> getArguments() {
        return args;
    }

    public boolean isDiscrete() {
        return d;
    }

    public String pretty(boolean verbose) {
        StringBuilder b = new StringBuilder();
        if (d) {
            b.append("discrete ");
        } else {
            b.append("continuous ");
        }
        b.append(c.toString(args)).append(" ");
        b.append(succeed()).append("\n");
        b.append("spec: ").append(expected).append("\n");
        b.append(verifier.toString()).append(": ").append(got);

        if (verbose) {
            b.append("\nSource Model:\n").append(plan.getOrigin().getMapping());
            if (plan != null) {
                b.append("Plan:\n").append(plan);
            } else {
                Model dst = plan.getResult();
                if (dst == null) {
                    b.append("Resulting model: -\n");
                } else {
                    b.append("Resulting model:\n").append(dst.getMapping());
                }

            }
        }
        return b.toString();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (d) {
            b.append("discrete ");
        }
        b.append(c.toString(args));
        b.append(' ');
        if (succeed()) {
            b.append(" valid");
        } else {
            b.append(" unconsistent (").append((falsePositive() ? "under-filtering" : "over-filtering")).append(")");
        }
        b.append(succeed());
        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestCase testCase = (TestCase) o;

        if (d != testCase.d) return false;
        if (!args.equals(testCase.args)) return false;
        if (!c.equals(testCase.c)) return false;
        if (!expected.equals(testCase.expected)) return false;
        if (!got.equals(testCase.got)) return false;
        if (plan != null ? !plan.equals(testCase.plan) : testCase.plan != null) return false;
        if (!verifier.toString().equals(testCase.verifier.toString())) return false;

        return true;
    }

    public boolean falsePositive() {
        return !expected.getStatus() && got.getStatus();
    }

    public boolean falseNegative() {
        return expected.getStatus() && !got.getStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(c, plan, args, expected, got, verifier, d);
    }

}
