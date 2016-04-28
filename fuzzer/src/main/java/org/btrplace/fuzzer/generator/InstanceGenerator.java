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

package org.btrplace.fuzzer.generator;

import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * @author Fabien Hermenier
 */
public class InstanceGenerator implements ModelParams, Supplier<Instance> {

    private Random rnd;

    private DefaultModelParams ps;

    public InstanceGenerator() {
        rnd = new Random();
        ps = new DefaultModelParams();
    }

    @Override
    public Instance get() {
        ModelGenerator gen = new ModelGenerator().setParams(ps);
        Model mo = gen.build();
        return new Instance(mo, states(mo), new MinMTTR());
    }

    @Override
    public int vms() {
        return ps.vms();
    }

    @Override
    public int nodes() {
        return ps.nodes();
    }

    @Override
    public ModelParams vms(int nb) {
        ps.vms(nb);
        return this;
    }

    @Override
    public ModelParams nodes(int nb) {
        ps.nodes(nb);
        return this;
    }

    private List<SatConstraint> states(Model mo) {
        List<SatConstraint> l = new ArrayList<>();
        for (VM vm : mo.getMapping().getAllVMs()) {
            int i = rnd.nextInt(5);
            switch (i) {
                case 0:
                    l.add(new Running(vm));
                    break;
                /*case 1:
                    l.add(new Sleeping(vm));
                    break;*/
                case 2:
                    l.add(new Ready(vm));
                    break;
                case 3:
                    l.add(new Killed(vm));

            }
        }
        for (Node n : mo.getMapping().getAllNodes()) {
            int i = rnd.nextInt(3);
            switch (i) {
                case 0:
                    l.add(new Online(n));
                    break;
                case 1:
                    l.add(new Offline(n));
                    break;

            }
        }
        return l;
    }
}
