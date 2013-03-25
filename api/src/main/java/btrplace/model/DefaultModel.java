/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation for {@link Model}.
 *
 * @author Fabien Hermenier
 */
public class DefaultModel implements Model, Cloneable {

    private Mapping cfg;

    private Map<String, ModelView> resources;

    private Attributes attrs;

    /**
     * Make a new instance using a particular mapping.
     *
     * @param m the mapping to use
     */
    public DefaultModel(Mapping m) {
        this.cfg = m;
        this.resources = new HashMap<String, ModelView>();
        attrs = new DefaultAttributes();
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
        Collection<ModelView> thatRrcs = that.getViews();
        return resources.size() == thatRrcs.size() && resources.values().containsAll(thatRrcs);
    }

    @Override
    public int hashCode() {
        int result = cfg.hashCode();
        result = 31 * result + resources.hashCode();
        result = 31 * result + attrs.hashCode();
        return result;
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
    public Model clone() {
        Model m = new DefaultModel(cfg.clone());
        for (ModelView rc : resources.values()) {
            m.attach(rc.clone());
        }
        m.setAttributes(this.getAttributes().clone());
        return m;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Mapping:\n");
        b.append(getMapping());
        b.append("\nAttributes:\n");
        b.append(getAttributes());
        b.append("\nViews:\n");
        for (Map.Entry<String, ModelView> entry : resources.entrySet()) {
            b.append(entry.getKey()).append(": ");
            b.append(entry.getValue()).append("\n");
        }
        return b.toString();
    }
}
