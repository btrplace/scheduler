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

import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.testing.TestCase;
import org.btrplace.safeplace.testing.Tester;
import org.btrplace.safeplace.testing.fuzzer.domain.Domain;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Fabien Hermenier
 */
public interface TestCaseFuzzer extends Supplier<TestCase>, ReconfigurationPlanParams {

    TestCaseFuzzer with(String var, int val);

    TestCaseFuzzer with(String var, int min, int max);

    TestCaseFuzzer with(String var, int [] vals);

    TestCaseFuzzer with(String var, String val);

    TestCaseFuzzer with(String var,  String[] vals);

    TestCaseFuzzer with(String var,  Domain d);

    TestCaseFuzzer validating(Constraint c, Tester t);

    TestCaseFuzzer restriction(Set<Restriction> domain);

    long lastFuzzingDuration();

    long lastValidationDuration();

    int lastFuzzingIterations();

    TestCaseFuzzer constraint(Constraint cstr);

    Constraint constraint();

    TestCaseFuzzer supportedConstraints(List<Constraint> cstrs);
}
