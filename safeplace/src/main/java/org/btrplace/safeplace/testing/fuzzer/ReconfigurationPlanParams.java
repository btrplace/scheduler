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

import org.btrplace.safeplace.testing.fuzzer.decorators.FuzzerDecorator;

/**
 * Possible customisation of a reconfiguration plan fuzzer.
 * @author Fabien Hermenier
 */
public interface ReconfigurationPlanParams {

    /**
     * Set the ratio of nodes initially offline.
     *
     * @param ratio a number <= 1.0
     * @return {@code this}
     */
    ReconfigurationPlanFuzzer srcOffNodes(double ratio);

    /**
     * Set the ratio of nodes offline at the end of the reconfiguration
     * @param ratio a number <= 1.0
     * @return {@code this}
     */
    ReconfigurationPlanFuzzer dstOffNodes(double ratio);

    /**
     * Set the distribution of VM initial state.
     * The sum of the ratio must be equals to 1
     * @param ready the ratio of VMs initially ready. <= 1.0
     * @param running the ratio of VMs initially running. <= 1.0
     * @param sleeping the ratio of VMs initially sleeping. <= 1.0
     * @return {@code this}
     */
    ReconfigurationPlanFuzzer srcVMs(double ready, double running, double sleeping);

    /**
     * Set the distribution of VM final state.
     * The sum of the ratio must be equals to 1
     * @param ready the ratio of VMs initially ready. <= 1.0
     * @param running the ratio of VMs initially running. <= 1.0
     * @param sleeping the ratio of VMs initially sleeping. <= 1.0
     * @return {@code this}
     */
    ReconfigurationPlanFuzzer dstVMs(double ready, double running, double sleeping);

    /**
     * Set the number of VMs inside the plan.
     * @param n a number >= 0
     * @return {@code this}
     */
    ReconfigurationPlanFuzzer vms(int n);

    /**
     * Set the number of nodes inside the plan.
     * @param n a number >= 0
     * @return {@code this}
     */
    ReconfigurationPlanFuzzer nodes(int n);

    /**
     * Set the bounds for the action duration
     * @param min the minimum duration
     * @param max the maximum duration
     * @return {@code this}
     */
    ReconfigurationPlanFuzzer durations(int min, int max);

    /**
     * Add a decorator.
     * It will be called once the initial plan generated.
     * @param f the decorator to add
     * @return {@code this}
     */
    ReconfigurationPlanFuzzer with(FuzzerDecorator f);
}
