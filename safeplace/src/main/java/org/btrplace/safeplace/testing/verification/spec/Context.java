/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
import java.util.Objects;

/**
 * @author Fabien Hermenier
 */
public class Context {


  private final SpecMapping sm;

  private final Map<String, Domain> vDoms;

  private final Model mo;

    private ReconfigurationPlan plan;

  private final LinkedList<Map<String, Object>> stack;

    private Context root;

    public Context() {
        this(new DefaultModel());
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

    public ReconfigurationPlan getPlan() {
        return plan;
    }

    public void setRootContext(Context root) {
        this.root = root;
    }

    public Context getRootContext() {
        return root;
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
      return Objects.hash(sm, vDoms, stack);
    }
}
