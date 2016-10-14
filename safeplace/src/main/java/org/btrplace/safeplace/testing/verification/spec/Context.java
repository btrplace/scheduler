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

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.safeplace.spec.type.NodeType;
import org.btrplace.safeplace.spec.type.VMType;
import org.btrplace.safeplace.testing.fuzzer.domain.ConstantDomain;
import org.btrplace.safeplace.testing.fuzzer.domain.Domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
        add(new ConstantDomain<>("nodes", NodeType.getInstance(), new ArrayList<>(mo.getMapping().getAllNodes())));
        add(new ConstantDomain<>("vms", VMType.getInstance(), new ArrayList<>(mo.getMapping().getAllVMs())));
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
        vDoms.put(d.name(), d);
    }

    public Domain domain(String lbl) {
        return vDoms.get(lbl);
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

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
