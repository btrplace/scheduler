/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

import org.btrplace.model.view.ModelView;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Default implementation for a {@link Model}.
 *
 * @author Fabien Hermenier
 */
public class DefaultModel implements Model {

  private final Mapping cfg;

  private final Map<String, ModelView> resources;

    private Attributes attrs;

  private final ElementBuilder elemBuilder;

    /**
     * Make a new instance that rely on a {@link DefaultElementBuilder}.
     */
    public DefaultModel() {
        this(new DefaultElementBuilder());
    }

    /**
     * Make a new instance relying on a given element builders.
     *
     * @param eb the builder to use
     */
    public DefaultModel(ElementBuilder eb) {
        this.resources = new HashMap<>();
        attrs = new DefaultAttributes();
        cfg = new DefaultMapping();
        elemBuilder = eb;
    }

    @Override
    public ModelView getView(String id) {
        return this.resources.get(id);
    }

    @Override
    public boolean attach(ModelView v) {
        if (this.resources.containsKey(v.getIdentifier())) {
            return false;
        }
        this.resources.put(v.getIdentifier(), v);
        return true;
    }

    @Override
    public Collection<ModelView> getViews() {
        return this.resources.values();
    }

    @Override
    public Mapping getMapping() {
        return this.cfg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Model that = (Model) o;

        if (!cfg.equals(that.getMapping())) {
            return false;
        }

        if (!attrs.equals(that.getAttributes())) {
            return false;
        }
        Collection<ModelView> thatViews = that.getViews();
        return resources.size() == thatViews.size() && resources.values().containsAll(thatViews);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cfg, resources, attrs);
    }

    @Override
    public boolean detach(ModelView v) {
        return resources.remove(v.getIdentifier()) != null;
    }

    @Override
    public void clearViews() {
        this.resources.clear();
    }

    @Override
    public Attributes getAttributes() {
        return attrs;
    }

    @Override
    public void setAttributes(Attributes a) {
        attrs = a;
    }

    @Override
    public Model copy() {
        DefaultModel m = new DefaultModel(elemBuilder.copy());
        MappingUtils.fill(cfg, m.cfg);
        for (ModelView rc : resources.values()) {
            m.attach(rc.copy());
        }
        m.setAttributes(this.getAttributes().copy());
        return m;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Mapping:\n");
        b.append(getMapping());
        b.append("\nAttributes:");
        if (!getAttributes().getDefined().isEmpty()) {
            b.append("\n");
            b.append(getAttributes());
        } else {
            b.append(" -");
        }
        b.append("\nViews:");
        if (resources.isEmpty()) {
            b.append(" -");
        } else {
            b.append("\n");
            StringJoiner joiner = new StringJoiner("\n");
            for (Map.Entry<String, ModelView> entry : resources.entrySet()) {
                joiner.add(String.format("%s: %s", entry.getKey(), entry.getValue()));
            }
            b.append(joiner.toString());
        }
        return b.toString();
    }

    @Override
    public VM newVM() {
        return elemBuilder.newVM();
    }

    @Override
    public VM newVM(int id) {
        return elemBuilder.newVM(id);
    }

    @Override
    public Node newNode() {
        return elemBuilder.newNode();
    }

    @Override
    public Node newNode(int id) {
        return elemBuilder.newNode(id);
    }

    @Override
    public boolean contains(VM v) {
        return elemBuilder.contains(v);
    }

    @Override
    public boolean contains(Node n) {
        return elemBuilder.contains(n);
    }
}
