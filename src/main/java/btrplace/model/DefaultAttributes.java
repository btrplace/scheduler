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

import java.util.*;

/**
 * Default implementation for {@link Attributes}.
 *
 * @author Fabien Hermenier
 */
public class DefaultAttributes implements Attributes, Cloneable {

    private Map<UUID, Map<String, Object>> attrs;

    /**
     * Make a new empty list of attributes.
     */
    public DefaultAttributes() {
        attrs = new HashMap<UUID, Map<String, Object>>();
    }

    @Override
    public Object set(UUID e, String k) {
        return set(e, k, Boolean.TRUE);
    }

    @Override
    public Object set(UUID e, String k, Object v) {
        Map<String, Object> m = attrs.get(e);
        if (m == null) {
            m = new HashMap<String, Object>();
            attrs.put(e, m);
        }
        return m.put(k, v);
    }

    @Override
    public boolean isSet(UUID e, String k) {
        Map<String, Object> m = attrs.get(e);
        if (m == null) {
            return false;
        }
        return m.containsKey(k);
    }

    @Override
    public Object unset(UUID e, String k) {
        Map<String, Object> m = attrs.get(e);
        if (m == null) {
            return null;
        }
        return m.remove(k);

    }

    @Override
    public Set<String> get(UUID e) {
        Map<String, Object> m = attrs.get(e);
        if (m == null) {
            return Collections.emptySet();
        }
        return m.keySet();
    }

    @Override
    public Object get(UUID e, String k) {
        Map<String, Object> m = attrs.get(e);
        if (m == null) {
            return null;
        }
        return m.get(k);
    }

    @Override
    public Attributes clone() {
        DefaultAttributes cpy = new DefaultAttributes();
        for (Map.Entry<UUID, Map<String, Object>> e : attrs.entrySet()) {
            cpy.attrs.put(e.getKey(), new HashMap<String, Object>(e.getValue()));
        }
        return cpy;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<UUID, Map<String, Object>> e : attrs.entrySet()) {
            b.append(e.getKey());
            b.append(':');
            for (Map.Entry<String, Object> attr : e.getValue().entrySet()) {
                b.append(" <").append(attr.getKey()).append(",").append(attr.getValue()).append('>');
            }
            b.append('\n');
        }
        return b.toString();
    }

    @Override
    public int hashCode() {
        return attrs.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!o.getClass().equals(getClass())) {
            return false;
        }
        DefaultAttributes that = (DefaultAttributes) o;
        return attrs.equals(that.attrs);
    }

    @Override
    public void clear() {
        this.attrs.clear();
    }
}
