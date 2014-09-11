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

package btrplace.solver.api.cstrSpec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Fabien Hermenier
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CstrTest {
    String[] groups() default {};

    String provider() default "";

    String input() default "";

    String constraint();
}


/**

 @CTest(replay="spread.json")
 public void testFoo(Verifier v) {
 v.timeout(5).maxFailures(1).maxTests(10000).workers(7);
 v.verify();
 }

 public ReconfigurationPlanFuzzer myFuzzer() {
 ReconfigurationPlanFuzzer f = new ReconfigurationPlanFuzzer();
 return f;
 }

 @CTest(fuzzer="myFuzzer")
 public void testBar(Verifier v) {

 }

 Phase 1, generate testcases for a given constraint. Need to integrate language extension

 Phase 2, run the testcases in a particular settings (type of verification)
 need to integration runtime extension (typically views, adapters & co
 */