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

package org.btrplace.safeplace.testing.verification.spec;

import org.btrplace.model.Node;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.*;
import org.btrplace.safeplace.spec.prop.Proposition;
import org.btrplace.safeplace.spec.type.NodeStateType;
import org.btrplace.safeplace.spec.type.VMStateType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class ReconfigurationSimulator implements ActionVisitor {

    private Context co;

    private ReconfigurationPlan p;

    private Map<Integer, List<Action>> starts;
    private Map<Integer, List<Action>> ends;

    private List<Integer> timeStamps;

    private boolean start = false;

    public ReconfigurationSimulator(Context origin, ReconfigurationPlan p) {
        co = origin;
        starts = new HashMap<>();
        ends = new HashMap<>();
        timeStamps = new ArrayList<>();
        this.p = p;
    }

    public int start(Proposition prop) {
        //sort actions by timestamp
        Set<Integer> s = new TreeSet<>(Comparator.comparingInt(a -> a));
        for (Action a : p.getActions()) {
            s.add(a.getStart());
            s.add(a.getEnd());
            if (!starts.containsKey(a.getStart())) {
                starts.put(a.getStart(), new ArrayList<>());
            }
            if (!ends.containsKey(a.getEnd())) {
                ends.put(a.getEnd(), new ArrayList<>());
            }

            starts.get(a.getStart()).add(a);
            ends.get(a.getEnd()).add(a);
        }
        timeStamps = s.stream().collect(Collectors.toList());

        for (Integer i : timeStamps) {
            List<Action> st = starts.get(i);
            if (st == null) {
                st = new ArrayList<>();
            }
            List<Action> ed = ends.get(i);
            if (ed == null) {
                ed = new ArrayList<>();
            }

            at(i, st, ed);
            Boolean res = prop.eval(co);
            if (!Boolean.TRUE.equals(res)) {
                return i;
            }
        }
        return -1;
    }

    private void at(Integer i, List<Action> starts, List<Action> ends) {
        //Apply all the actions simultaneously, starting by the ending

        start = false;
        for (Action a : ends) {
            a.visit(this);
        }

        start = true;
        for (Action a : starts) {
            a.visit(this);
        }
    }


    //The visitors
    @Override
    public Object visit(Allocate a) {
        return null;
    }

    @Override
    public Object visit(AllocateEvent a) {
        return null;
    }

    @Override
    public Object visit(SubstitutedVMEvent a) {
        return null;
    }

    @Override
    public Object visit(BootNode a) {
        if (start) {
            co.getMapping().state(a.getNode(), NodeStateType.Type.booting);
            return null;
        }
        co.getMapping().state(a.getNode(), NodeStateType.Type.online);
        return null;
    }

    @Override
    public Object visit(BootVM a) {
        if (start) {
            co.getMapping().state(a.getVM(), VMStateType.Type.booting);
            co.getMapping().host(a.getVM(), a.getDestinationNode());
            return null;
        }
        co.getMapping().state(a.getVM(), VMStateType.Type.running);
        return null;
    }

    @Override
    public Object visit(ForgeVM a) {
        return null;
    }

    @Override
    public Object visit(KillVM a) {
        if (start) {
            //TODO: terminating ?
            co.getMapping().state(a.getVM(), VMStateType.Type.terminated);
            return null;
        }

        Node n = a.getNode();
        if (n != null) {
            co.getMapping().unhost(n, a.getVM());
            co.getMapping().desactivate(a.getVM());
            co.getMapping().state(a.getVM(), VMStateType.Type.terminated);
        }

        return null;
    }

    @Override
    public Object visit(MigrateVM a) {
        if (start) {
            co.getMapping().state(a.getVM(), VMStateType.Type.migrating);
            co.getMapping().host(a.getVM(), a.getDestinationNode());
            return null;
        }
        //System.out.println("End " + a);
        co.getMapping().state(a.getVM(), VMStateType.Type.running);
        co.getMapping().activateOn(a.getVM(), a.getDestinationNode());
        //No longer hosted on the source node
        //running on the new one.
        co.getMapping().unhost(a.getSourceNode(), a.getVM());
        return null;
    }

    @Override
    public Object visit(ResumeVM a) {
        if (start) {
            co.getMapping().state(a.getVM(), VMStateType.Type.resuming);
            co.getMapping().host(a.getVM(), a.getDestinationNode());
            return null;
        }
        co.getMapping().state(a.getVM(), VMStateType.Type.running);
        return null;
    }

    @Override
    public Object visit(ShutdownNode a) {
        if (start) {
            co.getMapping().state(a.getNode(), NodeStateType.Type.halting);
            return null;
        }
        co.getMapping().state(a.getNode(), NodeStateType.Type.offline);
        return null;
    }

    @Override
    public Object visit(ShutdownVM a) {
        if (start) {
            co.getMapping().state(a.getVM(), VMStateType.Type.halting);
            return null;
        }
        co.getMapping().state(a.getVM(), VMStateType.Type.ready);
        co.getMapping().unhost(a.getNode(), a.getVM());
        co.getMapping().desactivate(a.getVM());
        return null;
    }

    @Override
    public Object visit(SuspendVM a) {
        if (start) {
            co.getMapping().state(a.getVM(), VMStateType.Type.suspending);
            return null;
        }
        co.getMapping().state(a.getVM(), VMStateType.Type.sleeping);
        return null;
    }
}
