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

package btrplace.safeplace.reducer;

import btrplace.safeplace.verification.TestCase;

/**
 * Reduce a test case as much as possible.
 * <p>
 * This is done as follow:
 * <ol>
 * <li>Un-necessary actions are removed</li>
 * <li>Un-necessary values in the constraints signature are removed</li>
 * <li>Un-necessary elements in the plan are removed</li>
 * </ol>
 *
 * @author Fabien Hermenier
 */
public class TestCaseReducer {

    private PlanReducer pr;

    private SignatureReducer sr;

    private ElementsReducer er;

    public TestCaseReducer() {
        pr = new PlanReducer();
        sr = new SignatureReducer();
        er = new ElementsReducer();
    }

    TestCase reduce(TestCase tc) throws Exception {
        //TestCase t = pr.reduce(tc);
        //List<Constant> reducedParams = sr.reduce(.getPlan(), tc.getConstraint(), tc.getArguments());
        //return er.reduce(t);
        //return new TestCase(tc.getVerifier(), tc.getConstraint(), reducedElements, reducedParams, tc.isDiscrete());
        return null;
    }
}
