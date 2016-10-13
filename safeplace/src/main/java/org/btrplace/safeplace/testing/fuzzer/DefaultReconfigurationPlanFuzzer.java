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

import org.btrplace.model.*;
import org.btrplace.model.constraint.*;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.*;
import org.btrplace.safeplace.testing.fuzzer.decorators.FuzzerDecorator;
import org.btrplace.safeplace.testing.verification.btrplace.Schedule;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlanFuzzer implements ReconfigurationPlanFuzzer {
    private Random rnd = new Random();

    private int nbNodes;
    private int nbVMs;

    private int minDuration;

    private int maxDuration;

    private double srcOffNodes;
    private double dstOffNodes;

    private double srcReadyVMs;
    private double srcRunningVMs;

    private double dstReadyVMs;
    private double dstRunningVMs;

    private List<FuzzerDecorator> exts;

    public DefaultReconfigurationPlanFuzzer() {
        //All the default values;
        nbNodes = 3;
        nbVMs = 3;
        minDuration = 1;
        maxDuration = 5;

        //Node state ratio
        srcOffNodes = 0.1;
        dstOffNodes = 0.1;

        //VM initial state ratio
        srcReadyVMs = 0.2;
        srcRunningVMs = 0.75;

        //VM destination states
        dstReadyVMs = 0.1;
        dstRunningVMs = 0.1;

        exts = new ArrayList<>();
    }

    @Override
    public ReconfigurationPlanFuzzer srcOffNodes(double ratio) {
        srcOffNodes = ratio;
        return this;
    }

    @Override
    public ReconfigurationPlanFuzzer dstOffNodes(double ratio) {
        dstOffNodes = ratio;
        return this;
    }

    @Override
    public ReconfigurationPlanFuzzer srcVMs(double ready, double running, double sleeping) {
        if (ready + running + sleeping != 1) {
            throw new IllegalArgumentException("The sum of the ratios should equals 1");
        }
        srcReadyVMs = ready;
        srcRunningVMs = running;
        return this;
    }

    @Override
    public ReconfigurationPlanFuzzer dstVMs(double ready, double running, double sleeping) {
        if (ready + running + sleeping != 1) {
            throw new IllegalArgumentException("The sum of the ratios should equals 1");
        }
        dstReadyVMs = ready;
        dstRunningVMs = running;
        return this;
    }

    public static int [] schedule(int min, int max, int makeSpan, Random rnd) {

        int duration = 1;
        if (min != max) {
            duration = rnd.nextInt(max - min) + min;
        }
        int st = makeSpan - duration;
        return new int[]{st, st + duration};
    }

    private int [] schedule() {
        int makeSpan = (maxDuration - minDuration) * (nbNodes + nbVMs);
        return schedule(minDuration, maxDuration, makeSpan, rnd);
    }

    @Override
    public ReconfigurationPlanFuzzer vms(int n) {
        nbVMs = n;
        return this;
    }

    @Override
    public ReconfigurationPlanFuzzer nodes(int n) {
        nbNodes = n;
        return this;
    }

    @Override
    public ReconfigurationPlanFuzzer durations(int min, int max) {
        minDuration = min;
        maxDuration = max;
        return this;
    }

    private <E extends Element>E pick(Collection<E> ns) {
        int x = rnd.nextInt(ns.size());
        Iterator<E> ite = ns.iterator();
        E n = null;
        while (x >= 0) {
            n = ite.next();
            x--;
        }
        return n;
    }

    private void addNode(Node n, ReconfigurationPlan p) {
        double src = rnd.nextDouble();
        double dst = rnd.nextDouble();
        int [] bounds = schedule();
        if (src <= srcOffNodes) {
            p.getOrigin().getMapping().addOfflineNode(n);
            if (dst > dstOffNodes) {
                p.add(new BootNode(n, bounds[0], bounds[1]));
                p.getOrigin().getAttributes().put(n, "boot", bounds[1] - bounds[0]);
            }
        } else {
            p.getOrigin().getMapping().addOnlineNode(n);
            if (dst <= srcOffNodes) {
                p.add(new ShutdownNode(n, bounds[0], bounds[1]));
                p.getOrigin().getAttributes().put(n, "shutdown", bounds[1] - bounds[0]);
            }
        }
    }

    private void addVM(VM v, ReconfigurationPlan p) {
        Mapping map = p.getOrigin().getMapping();
        Set<Node> onlines = map.getOnlineNodes();
        Node host = null;
        if (onlines.isEmpty()) {
            map.addReadyVM(v);
        } else {
            double n = rnd.nextDouble();
            if (n <= srcReadyVMs) {
                map.addReadyVM(v);
            }if (n <= srcReadyVMs + srcRunningVMs) {
                host = pick(onlines);
                map.addRunningVM(v, host);
            } else {
                host = pick(onlines);
                map.addSleepingVM(v, host);
            }
        }

        //Destination state
        double n = rnd.nextDouble();
        int [] bounds = schedule();
        if (n <= dstReadyVMs) {
            if (host != null) {
                p.add(new ShutdownVM(v, host, bounds[0], bounds[1]));
                p.getOrigin().getAttributes().put(v, "shutdown", bounds[1] - bounds[0]);
            }
        } else if (n <= dstReadyVMs + dstRunningVMs) {
            if (host == null ) {
                p.add(new BootVM(v, pick(map.getAllNodes()), bounds[0], bounds[1]));
                p.getOrigin().getAttributes().put(v, "boot", bounds[1] - bounds[0]);
            } else {
                //was running -> migrate
                if (map.isRunning(v)) {
                    Node dst = pick(map.getAllNodes());
                    if (!dst.equals(host)) {
                        p.add(new MigrateVM(v, host, dst, bounds[0], bounds[1]));
                        p.getOrigin().getAttributes().put(v, "migrate", bounds[1] - bounds[0]);
                    }
                } else {
                    //was sleeping -> resume
                    p.add(new ResumeVM(v, host, pick(map.getAllNodes()), bounds[0], bounds[1]));
                    p.getOrigin().getAttributes().put(v, "resume", bounds[1] - bounds[0]);
                }

            }
        } else {
            //moving to sleeping state
            if (map.isRunning(v)) {
                p.add(new SuspendVM(v, host, host, bounds[0], bounds[1]));
                p.getOrigin().getAttributes().put(v, "sleeping", bounds[1] - bounds[0]);
            }
        }
    }

    @Override
    public ReconfigurationPlan get() {
        Model mo = new DefaultModel();
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);

        for (int i = 0; i < nbNodes; i++) {
            addNode(mo.newNode(), p);
        }
        for (int i = 0; i < nbVMs; i++) {
            addVM(mo.newVM(), p);
        }

        exts.forEach(d -> d.decorate(p));
        return p;
    }

    @Override
    public ReconfigurationPlanFuzzer with(FuzzerDecorator f) {
        exts.add(f);
        return this;
    }

    @Override
    public Instance toInstance(ReconfigurationPlan p) {
        Model mo = p.getOrigin().copy();
        List<SatConstraint> cstrs = new ArrayList<>();

        Set<VM> knownVMs = new HashSet<>();
        Set<Node> knownNodes = new HashSet<>();
        for (Action a : p.getActions()) {

            if (a instanceof NodeEvent) {
                Node n = ((NodeEvent)a).getNode();
                knownNodes.add(n);
                cstrs.add(new Schedule(n, a.getStart(), a.getEnd()));

                if (a instanceof BootNode) {cstrs.add(new Online(n));}
                else if (a instanceof ShutdownNode) {cstrs.add(new Offline(n));}
            }

            else if (a instanceof VMEvent) {
                VM v = ((VMEvent)a).getVM();
                knownVMs.add(v);
                cstrs.add(new Schedule(v, a.getStart(), a.getEnd()));

                if (a instanceof BootVM) {cstrs.add(new Running(v)); cstrs.add(new Fence(v, ((BootVM) a).getDestinationNode()));}
                else if (a instanceof MigrateVM) {knownVMs.remove(v); cstrs.add(new Fence(v, ((MigrateVM) a).getDestinationNode()));}
                else if (a instanceof ShutdownVM) {cstrs.add(new Ready(v));}
                else if (a instanceof SuspendVM) {cstrs.add(new Sleeping(v));}
                else if (a instanceof ResumeVM) {cstrs.add(new Running(v)); cstrs.add(new Fence(v, ((ResumeVM) a).getDestinationNode()));}
                else if (a instanceof Allocate) {
                    cstrs.add(new Preserve(v, ((Allocate) a).getResourceId(), ((Allocate) a).getAmount()));
                    cstrs.add(new Fence(v, ((Allocate) a).getHost()));
                }
            }
            //Catch the allocate events
            for (Event e : a.getEvents(Action.Hook.PRE)) {
                if (e instanceof AllocateEvent) {
                    cstrs.add(new Preserve(((AllocateEvent) e).getVM(), ((AllocateEvent) e).getResourceId(), ((AllocateEvent) e).getAmount()));
                }
            }
            for (Event e : a.getEvents(Action.Hook.POST)) {
                if (e instanceof AllocateEvent) {
                    cstrs.add(new Preserve(((AllocateEvent) e).getVM(), ((AllocateEvent) e).getResourceId(), ((AllocateEvent) e).getAmount()));
                }
            }

        }

        //State maintenance
        for (Node n : mo.getMapping().getAllNodes()) {
            if (knownNodes.contains(n)) {
                continue;
            }

            if (mo.getMapping().isOnline(n)) {
                cstrs.add(new Online(n));
            } else if (mo.getMapping().isOffline(n)) {
                cstrs.add(new Offline(n));
            }
        }
        mo.getMapping().getAllVMs().stream().filter(v -> !knownVMs.contains(v)).forEach(
            v -> {
            if (mo.getMapping().isReady(v)) {cstrs.add(new Ready(v));}
            else if (mo.getMapping().isRunning(v)) {cstrs.add(new Running(v)); cstrs.add(new Fence(v, mo.getMapping().getVMLocation(v)));}
            else if (mo.getMapping().isSleeping(v)) {cstrs.add(new Sleeping(v)); cstrs.add(new Fence(v, mo.getMapping().getVMLocation(v)));}
        });

        return new Instance(mo, cstrs, new MinMTTR());
    }
}
