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

package btrplace.safeplace;

import btrplace.plan.ReconfigurationPlan;
import btrplace.safeplace.spec.term.Constant;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class CTestCase {

    private String testName;

    private Class testClass;

    private Constraint cstr;

    private List<Constant> args;

    private ReconfigurationPlan plan;

    private boolean continuous;

    private int nb;
    private int number;

    public CTestCase(Class clName, String testName, int nb, Constraint cstr, List<Constant> argv, ReconfigurationPlan p, boolean c) {
        testClass = clName;
        this.testName = testName;
        this.nb = nb;
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
        return testClass.getSimpleName() + "." + testName + "#" + nb;
    }

    public Class getTestClass() {
        return testClass;
    }

    public String getTestName() {
        return testName;
    }

    public int getNumber() {
        return nb;
    }
}
