/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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


import org.btrplace.safeplace.testing.fuzzer.decorators.FuzzerDecorator;
import org.btrplace.safeplace.testing.fuzzer.domain.Domain;

import java.io.Writer;
import java.util.Set;

/**
 * A configurable fuzzer where it is possible to configure the {@link org.btrplace.plan.ReconfigurationPlan}
 * but also the constraint arguments.
 * @author Fabien Hermenier
 */
public interface ConfigurableFuzzer extends Fuzzer {

    /**
     * Set the value for a constraint int argument.
     *
     * @param arg the argument name
     * @param val the value
     * @return {@code this}
     */
    ConfigurableFuzzer with(String arg, int val);

    /**
     * Set the domain for a constraint int argument.
     * The fuzzer will pick a value among this domain
     *
     * @param arg the argument name
     * @param min the lower bound
     * @param max the upper bound
     * @return {@code this}
     */
    ConfigurableFuzzer with(String arg, int min, int max);

    /**
     * Set the domain for a constraint int argument.
     * The fuzzer will pick a value among this domain
     *
     * @param arg  the argument name
     * @param vals the possible values
     * @return {@code this}
     */
    ConfigurableFuzzer with(String arg, int[] vals);

    /**
     * Set the value for a constraint String argument.
     *
     * @param arg the argument name
     * @param val the value
     * @return {@code this}
     */
    ConfigurableFuzzer with(String arg, String val);

    /**
     * Set the domain for a constraint String argument.
     * The fuzzer will pick a value among this domain
     *
     * @param arg  the argument name
     * @param vals the possible values
     * @return {@code this}
     */
    ConfigurableFuzzer with(String arg, String[] vals);

    /**
     * Set the domain for a constraint argument.
     * The fuzzer will pick a value among this domain
     *
     * @param arg the argument name
     * @param d   the argument domain
     * @return {@code this}
     */
    ConfigurableFuzzer with(String arg, Domain d);

    /**
     * Set the restriction domain for the constraint to fuzz.
     * The fuzzer will pick a value among this domain
     *
     * @param domain the domain
     * @return {@code this}
     */
    ConfigurableFuzzer restriction(Set<Restriction> domain);

    /**
     * Write the generated test cases for a later replay.
     * Each {@link org.btrplace.safeplace.testing.TestCase} is serialised to a JSON format.
     * One line per test case.
     * @see Replay to provide the saved test cases
     * @param w the output stream
     * @return {@code this}
     */
    ConfigurableFuzzer save(Writer w);

    /**
     * Write the generated test cases for a later replay.
     *
     * @see #save(Writer)
     * @param path the output file
     * @return {@code this}
     */
    ConfigurableFuzzer save(String path);

    /**
     * Set the ratio of nodes initially OFFLINE.
     *
     * @param ratio a number <= 1.0
     * @return {@code this}
     */
    ConfigurableFuzzer srcOffNodes(double ratio);

    /**
     * Set the ratio of nodes OFFLINE at the END of the reconfiguration
     *
     * @param ratio a number <= 1.0
     * @return {@code this}
     */
    ConfigurableFuzzer dstOffNodes(double ratio);

    /**
     * Set the distribution of VM initial state.
     * The individual weight compared to their sum indicates the probability.
     * For example, if the cumulative weight is 100, then each weight denotes a percentage
     *
     * @param ready    the weight of VMs initially ready.
     * @param running  the weight of VMs initially running.
     * @param sleeping the weight of VMs initially sleeping.
     * @return {@code this}
     */
    ConfigurableFuzzer srcVMs(int ready, int running, int sleeping);

    /**
     * Set the distribution of VM final state.
     * The individual weight compared to their sum indicates the probability.
     * For example, if the cumulative weight is 100, then each weight denotes a percentage
     *
     * @param ready    the weight of VMs eventually ready.
     * @param running  the weight of VMs eventually running.
     * @param sleeping the weight of VMs eventually sleeping.
     * @return {@code this}
     */
    ConfigurableFuzzer dstVMs(int ready, int running, int sleeping);

    /**
     * Set the number of VMs inside the plan.
     *
     * @param n a number >= 0
     * @return {@code this}
     */
    ConfigurableFuzzer vms(int n);

    /**
     * Set the number of nodes inside the plan.
     *
     * @param n a number >= 0
     * @return {@code this}
     */
    ConfigurableFuzzer nodes(int n);

    /**
     * Set the bounds for the action duration
     *
     * @param min the minimum duration
     * @param max the maximum duration
     * @return {@code this}
     */
    ConfigurableFuzzer durations(int min, int max);

    /**
     * Add a decorator.
     * It will be called once the initial plan generated.
     *
     * @param f the decorator to add
     * @return {@code this}
     */
    ConfigurableFuzzer with(FuzzerDecorator f);
}
