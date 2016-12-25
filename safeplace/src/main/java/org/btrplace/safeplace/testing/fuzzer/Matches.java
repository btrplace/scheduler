/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.safeplace.testing.fuzzer;

import org.btrplace.json.model.InstanceConverter;
import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.testing.Result;
import org.btrplace.safeplace.testing.TestCase;
import org.btrplace.safeplace.testing.TestCaseResult;
import org.btrplace.safeplace.testing.Tester;
import org.btrplace.safeplace.testing.verification.btrplace.ScheduleConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Fabien Hermenier
 */
public class Matches implements Predicate<TestCase> {

    private List<Constraint> cstrs;

    private Tester tester;

    private long duration;

    public Matches(Tester t) {
        cstrs = new ArrayList<>();
        tester = t;
    }

    public Matches() {
        this(null);
    }

    public Matches with(Constraint ...cstrs) {
        Collections.addAll(this.cstrs, cstrs);
        return this;
    }

    public Matches setTester(Tester t) {
        tester = t;
        return this;
    }
    @Override
    public boolean test(TestCase o) {
        duration = -System.currentTimeMillis();

        if (tester != null) {
            for (Constraint c : cstrs) {
                TestCase tc = purge(o, c);
                TestCaseResult res = tester.test(tc);
                if (res.result() != Result.success) {
                    duration += System.currentTimeMillis();
                    InstanceConverter ic = new InstanceConverter();
                    ic.getConstraintsConverter().register(new ScheduleConverter());
                    /*try {
                        System.out.println(ic.toJSON(tc.instance()));
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }*/
                    return false;
                }
            }
        }
        duration += System.currentTimeMillis();
        return true;
    }

    private TestCase purge(TestCase o, Constraint c) {
        //Get rid of all the views (optional so subject to un-awaited fault injection
        /*Model m = o.instance().getModel();
        m.getViews().clear();
        o.plan().getOrigin().getViews().clear();
        for (Iterator<SatConstraint> ite = o.instance().getSatConstraints().iterator(); ite.hasNext(); ) {
            SatConstraint cstr = ite.next();
            if (cstr instanceof Preserve) {
                ite.remove();
            }
        }
        */
        return new TestCase(o.instance(), o.plan(), c);
    }


    public long lastDuration() {
        return duration;
    }
}
