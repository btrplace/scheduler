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

package org.btrplace.safeplace.testing.fuzzer.decorators;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.*;

import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class ShareableResourceFuzzer implements FuzzerDecorator {

    private String id;

    private Random rnd;

    private int minCons, maxCons, minCapa, maxCapa;

    private double variability = 0.5;

    public ShareableResourceFuzzer(String rc, int minCons, int maxCons, int minCapa, int maxCapa) {
        id = rc;
        rnd = new Random();
        this.minCons = minCons;
        this.minCapa = minCapa;
        this.maxCapa = maxCapa;
        this.maxCons = maxCons;
    }

    public ShareableResourceFuzzer variability(double v) {
        variability = v;
        return this;
    }

    @Override
    public ShareableResourceFuzzer copy() {
        return new ShareableResourceFuzzer(id, minCons, maxCons, minCapa, maxCapa);
    }

    @Override
    public void decorate(Model mo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void decorate(ReconfigurationPlan p) {
        Model mo = p.getOrigin();
        ShareableResource rc = new ShareableResource(id);
        mo.attach(rc);

        Set<VM> toRun = p.getActions().stream()
                .filter(a -> a instanceof RunningVMPlacement)
                .map(a -> ((RunningVMPlacement) a).getVM())
                .collect(Collectors.toSet());
        //Initial consumption/capacity
        for (VM v : mo.getMapping().getAllVMs()) {
            int c = rnd.nextInt(maxCons - minCons + 1) + minCons;
            rc.setConsumption(v, c);
            //System.err.println(v + " src=" + rc.getConsumption(v));
        }

        for (Node n : mo.getMapping().getAllNodes()) {
            int c = rnd.nextInt(maxCapa - minCapa + 1) + minCapa;
            rc.setCapacity(n, c);
        }

        //New consumption
        //No ! otherwise multiple allocate so no way to test one.
        //Composition issue
        for (VM v : toRun) {
            if (rnd.nextDouble() > variability) {
                continue;
            }
            int c = rnd.nextInt(maxCons - minCons + 1) + minCons;
            setDemand(p, rc, v, c);
        }
    }

    private void setDemand(ReconfigurationPlan p, ShareableResource rc,  VM v, int c) {
        if (c == rc.getConsumption(v)) {
            return;
        }
        boolean found = false;
        for (Action a : p.getActions()) {
            if (a instanceof VMEvent && ((VMEvent) a).getVM().equals(v) && a instanceof RunningVMPlacement) {
                AllocateEvent ev = new AllocateEvent(v, id, c);

                Action.Hook h = Action.Hook.PRE;
                if (a instanceof MigrateVM) {
                    h = Action.Hook.POST;
                }
                //For a migrated VM, we allocate once the migration over
                a.addEvent(h, ev);
                //System.err.println(a + " " + h + " " + ev);
                found = true;
                break;
            }
        }
        if (!found && p.getOrigin().getMapping().isRunning(v)) {
            int d = 1;
            if (p.getDuration() > 0) {
                d = rnd.nextInt(p.getDuration());
            }
            p.add(new Allocate(v, p.getOrigin().getMapping().getVMLocation(v), id, c, d, d + 1));
            p.getOrigin().getAttributes().put(v, "allocate", 1);
        }
    }
}
