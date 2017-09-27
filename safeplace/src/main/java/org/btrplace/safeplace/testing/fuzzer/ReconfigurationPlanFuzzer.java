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

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Element;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.BootNode;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.ResumeVM;
import org.btrplace.plan.event.ShutdownNode;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.plan.event.SuspendVM;
import org.btrplace.safeplace.testing.fuzzer.decorators.FuzzerDecorator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A Fuzzer to generate {@link DefaultReconfigurationPlan}.
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanFuzzer implements Supplier<ReconfigurationPlan> {
    private Random rnd;

    private int nbNodes;
    private int nbVMs;

    private int minDuration;

    private int maxDuration;

    private double srcOffNodes;
    private double dstOffNodes;

    private int srcReadyVMs;
    private int srcRunningVMs;
    private int srcSleepingVMs;

    private int dstReadyVMs;
    private int dstRunningVMs;
    private int dstSleepingVMs;

    private List<FuzzerDecorator> exts;

    public ReconfigurationPlanFuzzer(Random rnd) {
        this.rnd = rnd;
        //all the default values
        nbNodes = 3;
        nbVMs = 3;
        minDuration = 1;
        maxDuration = 5;

        //Node state ratio
        srcOffNodes = 0.1;
        dstOffNodes = 0.1;

        //VM initial state ratio
        srcReadyVMs = 20;
        srcRunningVMs = 75;
        srcSleepingVMs = 5;

        //VM destination states
        dstReadyVMs = 20;
        dstRunningVMs = 75;
        dstRunningVMs = 5;

        exts = new ArrayList<>();
    }

    /**
     * Make a new fuzzer with default values.
     */
    public ReconfigurationPlanFuzzer() {
        this(new Random());
    }


    public ReconfigurationPlanFuzzer srcOffNodes(double ratio) {
        srcOffNodes = ratio;
        return this;
    }


    public ReconfigurationPlanFuzzer dstOffNodes(double ratio) {
        dstOffNodes = ratio;
        return this;
    }


    public ReconfigurationPlanFuzzer srcVMs(int ready, int running, int sleeping) {
        srcReadyVMs = ready;
        srcRunningVMs = running;
        srcSleepingVMs = sleeping;
        return this;
    }


    public ReconfigurationPlanFuzzer dstVMs(int ready, int running, int sleeping) {
        dstReadyVMs = ready;
        dstRunningVMs = running;
        dstSleepingVMs = sleeping;
        return this;
    }

    public static int[] schedule(int min, int max, int makeSpan, Random rnd) {

        int duration = 1;
        if (min != max) {
            duration = rnd.nextInt(max - min) + min;
        }
        int st = rnd.nextInt(makeSpan - duration + 1);
        return new int[]{st, st + duration};
    }

    private int[] schedule() {
        int makeSpan = (maxDuration - minDuration) * (nbNodes + nbVMs);
        return schedule(minDuration, maxDuration, makeSpan, rnd);
    }


    public ReconfigurationPlanFuzzer vms(int n) {
        nbVMs = n;
        return this;
    }


    public ReconfigurationPlanFuzzer nodes(int n) {
        nbNodes = n;
        return this;
    }


    public ReconfigurationPlanFuzzer durations(int min, int max) {
        minDuration = min;
        maxDuration = max;
        return this;
    }

    private <E extends Element> E pick(Collection<E> ns) {
        int x = rnd.nextInt(ns.size());
        Iterator<E> ite = ns.iterator();
        E n = null;
        while (x >= 0) {
            n = ite.next();
            x--;
        }
        return n;
    }

    private boolean addNode(Node n, ReconfigurationPlan p) {
        double src = rnd.nextDouble();
        double dst = rnd.nextDouble();
        int[] bounds = schedule();
        if (src < srcOffNodes) {
            p.getOrigin().getMapping().addOfflineNode(n);
            if (dst > dstOffNodes) {
                p.add(new BootNode(n, bounds[0], bounds[1]));
                p.getOrigin().getAttributes().put(n, "boot", bounds[1] - bounds[0]);
                return true;
            }
        } else {
            p.getOrigin().getMapping().addOnlineNode(n);
            if (dst < srcOffNodes) {
                p.add(new ShutdownNode(n, bounds[0], bounds[1]));
                p.getOrigin().getAttributes().put(n, "shutdown", bounds[1] - bounds[0]);
            }
        }
        return false;
    }

    private void addVM(VM v, ReconfigurationPlan p) {
        setInitialState(p, v);
        setDestinationState(p, v);
    }

    private void setDestinationState(ReconfigurationPlan p, VM v) {
        Mapping map = p.getOrigin().getMapping();
        Node host = map.getVMLocation(v);

        int n = rnd.nextInt(dstReadyVMs + dstRunningVMs + dstSleepingVMs);
        int[] bounds = schedule();
        int duration = bounds[1] - bounds[0];
        if (n < dstReadyVMs) {
            if (host != null) {
                p.add(new ShutdownVM(v, host, bounds[0], bounds[1]));
                p.getOrigin().getAttributes().put(v, "shutdown", duration);
            }
        } else if (n < dstReadyVMs + dstRunningVMs) {
            if (host == null) {
                p.add(new BootVM(v, pick(map.getAllNodes()), bounds[0], bounds[1]));
                p.getOrigin().getAttributes().put(v, "boot", duration);
            } else {
                //was running -> migrate
                if (map.isRunning(v)) {
                    Node dst = pick(map.getAllNodes());
                    if (!host.equals(dst)) {
                        p.add(new MigrateVM(v, host, dst, bounds[0], bounds[1]));
                        p.getOrigin().getAttributes().put(v, "migrate", duration);
                    }
                } else {
                    //was sleeping -> resume
                    p.add(new ResumeVM(v, host, pick(map.getAllNodes()), bounds[0], bounds[1]));
                    p.getOrigin().getAttributes().put(v, "resume", duration);
                }
            }
        } else {
            //moving to sleeping state
            if (map.isRunning(v)) {
                p.add(new SuspendVM(v, host, host, bounds[0], bounds[1]));
                p.getOrigin().getAttributes().put(v, "sleeping", duration);
            }
        }
    }

    private void setInitialState(ReconfigurationPlan p, VM v) {
        Mapping map = p.getOrigin().getMapping();
        Set<Node> onlines = map.getOnlineNodes();
        if (onlines.isEmpty()) {
            map.addReadyVM(v);
            return;
        }
        //CDF to consider the distribution
        int n = rnd.nextInt(srcReadyVMs + srcRunningVMs + srcSleepingVMs);
        if (n < srcReadyVMs) {
            map.addReadyVM(v);
        } else if (n < srcReadyVMs + srcRunningVMs) {
            map.addRunningVM(v, pick(onlines));
        } else {
            map.addSleepingVM(v, pick(onlines));
        }
    }


    @Override
    public ReconfigurationPlan get() {
        Model mo = new DefaultModel();
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);

        for (int i = 0; i < nbNodes; i++) {
            Node n = mo.newNode();
            addNode(n, p);
        }
        for (int i = 0; i < nbVMs; i++) {
            addVM(mo.newVM(), p);
        }

        exts.forEach(d -> d.decorate(p));
        return p;
    }


    public ReconfigurationPlanFuzzer with(FuzzerDecorator f) {
        exts.add(f);
        return this;
    }

}
