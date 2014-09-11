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

import btrplace.safeplace.spec.SpecExtractor;

/**
 * @author Fabien Hermenier
 */
public class ParallelConstraintVerificationFuzzTest {

    public Specification getSpec() throws Exception {
        SpecExtractor r = new SpecExtractor();
        return r.extract();
    }

    /*@Test
    public void testFuzz() throws Exception {
        String root = "src/main/bin/";
        Specification s = getSpec();
        ReconfigurationPlanFuzzer2 fuzz = new ReconfigurationPlanFuzzer2();
        Constraint c = s.get("maxOnline");
        System.out.println(c.pretty());
        List<VerifDomain> doms = new ArrayList<>();
        doms.add(new IntVerifDomain(0, 5));
        ParallelConstraintVerificationFuzz pc = new ParallelConstraintVerificationFuzz(fuzz, doms, new ImplVerifier(), c);
        ReducedDefiantStore b = new ReducedDefiantStore();
        b.reduceWith(new PlanReducer());
        b.reduceWith(new ElementsReducer());
        pc.setBackend(b);
        pc.limit(new MaxTestsGuard(10000));
        //pc.limit(new TimeGuard(60));
        pc.setNbWorkers(1);
        pc.setContinuous(true);
        for (Constraint x : s.getConstraints()) {
            if (x.isCore() && x != c) {
                pc.precondition(x);
            }
        }
        pc.verify();
        int nb = b.getDefiant().size() + b.getCompliant().size();
        System.out.println(b.getDefiant().size() + "/" + nb);

        int falseOk = 0, falseKo = 0;

        for (TestCase tc : b.getDefiant()) {
            //    System.out.println(tc.pretty(true));
            if (tc.falsePositive()) {
                falseOk++;
            } else if (tc.falseNegative()) {
                falseKo++;
            } else {
                System.err.println("Buggy: " + tc.pretty(false));
            }
        }
        System.out.println(falseOk + "false positives; " + falseKo + " false negatives");
        Assert.fail();
    }              */
}
