/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
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

package org.btrplace.safeplace.verification.spec;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.safeplace.spec.type.NodeStateType;
import org.btrplace.safeplace.spec.type.VMStateType;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class Context {

    private SpecMapping sm;

    private Map<String, Domain> vDoms;

    private Model mo;

    private ReconfigurationPlan plan;

    private LinkedList<Map<String, Object>> stack;

    public Context() {
        this(new DefaultModel());
    }

    public ReconfigurationPlan getPlan() {
        return plan;
    }

    public Context(Model mo) {
        this.mo = mo;
        sm = new SpecMapping(mo.getMapping());
        vDoms = new HashMap<>();
        stack = new LinkedList<>();
        stack.add(new HashMap<>());
        //default domains
        add(new SetDomain<>("nodes", mo.getMapping().getAllNodes()));
        add(new SetDomain<>("vms", mo.getMapping().getAllVMs()));
        add(new SetDomain<>("vmState", EnumSet.allOf(VMStateType.Type.class)));
        add(new SetDomain<>("nodeState", EnumSet.allOf(NodeStateType.Type.class)));
    }

    public Model getModel() {
        return mo;
    }

    public SpecMapping getMapping() {
        return sm;
    }

    public void setValue(String label, Object o) {
        stack.getFirst().put(label, o);
    }

    public Object getValue(String label) {
        return stack.getFirst().get(label);
    }

    public void add(Domain d) {
        vDoms.put(d.type(), d);
    }

    public Set domain(String lbl) {
        Domain v = vDoms.get(lbl);
        if (v == null) {
            return null;
        }
        return v.values();
    }

    @Override
    public String toString() {
        return stack.toString();
    }

    public void saveStack() {
        stack.push(new HashMap<>());
    }

    public void restoreStack() {
        stack.pop();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Context context = (Context) o;

        return sm.equals(context.sm);
    }

    @Override
    public int hashCode() {
        int result = sm.hashCode();
        result = 31 * result + vDoms.hashCode();
        result = 31 * result + stack.hashCode();
        return result;
    }
}
