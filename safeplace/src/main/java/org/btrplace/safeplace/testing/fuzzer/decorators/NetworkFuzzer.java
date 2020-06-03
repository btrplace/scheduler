/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

  private final Random rnd;

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
