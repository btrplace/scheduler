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

import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.*;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.*;
import org.btrplace.safeplace.testing.verification.btrplace.Schedule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class to convert a plan to an instance.
 *
 * @author Fabien Hermenier
 */
public class InstanceConverter {

    private InstanceConverter() {
    }

    public static Instance toInstance(ReconfigurationPlan p) {
        Model mo = p.getOrigin().copy();
        List<SatConstraint> cstrs = new ArrayList<>();

        Set<VM> knownVMs = new HashSet<>();
        Set<Node> knownNodes = new HashSet<>();
        for (Action a : p.getActions()) {

            if (a instanceof NodeEvent) {
                Node n = ((NodeEvent) a).getNode();
                knownNodes.add(n);
                cstrs.add(new Schedule(n, a.getStart(), a.getEnd()));

                if (a instanceof BootNode) {
                    cstrs.add(new Online(n));
                } else if (a instanceof ShutdownNode) {
                    cstrs.add(new Offline(n));
                }
            } else if (a instanceof VMEvent) {
                VM v = ((VMEvent) a).getVM();
                knownVMs.add(v);
                cstrs.add(new Schedule(v, a.getStart(), a.getEnd()));
                if (a instanceof BootVM) {
                    cstrs.add(new Running(v));
                    cstrs.add(new Fence(v, ((BootVM) a).getDestinationNode()));
                } else if (a instanceof MigrateVM) {
                    cstrs.add(new Fence(v, ((MigrateVM) a).getDestinationNode()));
                    cstrs.add(new Running(v));
                } else if (a instanceof ShutdownVM) {
                    cstrs.add(new Ready(v));
                } else if (a instanceof SuspendVM) {
                    cstrs.add(new Sleeping(v));
                } else if (a instanceof ResumeVM) {
                    cstrs.add(new Running(v));
                    cstrs.add(new Fence(v, ((ResumeVM) a).getDestinationNode()));
                } else if (a instanceof Allocate) {
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
                    if (mo.getMapping().isReady(v)) {
                        cstrs.add(new Ready(v));
                    } else if (mo.getMapping().isRunning(v)) {
                        cstrs.add(new Running(v));
                        cstrs.add(new Fence(v, mo.getMapping().getVMLocation(v)));
                    } else if (mo.getMapping().isSleeping(v)) {
                        cstrs.add(new Sleeping(v));
                        cstrs.add(new Fence(v, mo.getMapping().getVMLocation(v)));
                    }
                });

        return new Instance(mo, cstrs, new MinMTTR());
    }
}
