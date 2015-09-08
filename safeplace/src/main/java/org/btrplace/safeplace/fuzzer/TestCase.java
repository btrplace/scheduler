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

package org.btrplace.safeplace.fuzzer;

import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.safeplace.Constraint;
import org.btrplace.safeplace.spec.term.Constant;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class TestCase {

    private String testName;

    private Constraint cstr;

    private List<Constant> args;

    private ReconfigurationPlan plan;

    private boolean continuous;

    public TestCase(String testName, Constraint cstr, List<Constant> argv, ReconfigurationPlan p, boolean c) {
        this.testName = testName;
        this.cstr = cstr;
        args = argv;
        plan = p;
        continuous = c;
    }

    public boolean continuous() {
        return continuous;
    }

    @Override
    public String toString() {
        return "id: " + id() +
                "\nConstraint: " + cstr.toString(args) +
                "\nContinuous: " + continuous() +
                "\nOrigin:\n" + plan.getOrigin().getMapping() +
                "Plan:\n" + plan;
    }

    public Constraint getConstraint() {
        return cstr;
    }

    public List<Constant> getParameters() {
        return args;
    }

    public ReconfigurationPlan getPlan() {
        return plan;
    }

    public String id() {
        return cstr.getClassName() + "#" + testName;
    }

    public String getTestName() {
        return testName;
    }
}
