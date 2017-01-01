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

package org.btrplace.safeplace.testing.fuzzer.decorators;

import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.model.view.network.Network;
import org.btrplace.model.view.network.Switch;
import org.btrplace.plan.ReconfigurationPlan;

import java.util.Random;

/**
 * A fuzzer than generate a network with a single switch.
 * The switch capacity and the network link bandwidht are between 1 and 40 GBit.
 * @author Fabien Hermenier
 */
public class NetworkFuzzer implements FuzzerDecorator {

    private Random rnd;

    public NetworkFuzzer() {
        rnd = new Random();
    }

    @Override
    public void decorate(ReconfigurationPlan p) {
        Network net = new Network();
        Mapping m = p.getOrigin().getMapping();
        Switch s = net.newSwitch(1000 * (1 + rnd.nextInt(40)));
        for (Node n : m.getAllNodes()) {
            int bw = 1000 * (1 + rnd.nextInt(40));
            net.connect(bw, s, n);
        }
    }
}
