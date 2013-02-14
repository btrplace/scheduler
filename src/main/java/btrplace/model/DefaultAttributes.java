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

    private boolean putObject(UUID e, String k, Object v) {
        Map<String, Object> m = attrs.get(e);
        if (m == null) {
            m = new HashMap<String, Object>();
            attrs.put(e, m);
        }
        return m.put(k, v) != null;
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
    public boolean isSet(UUID e, String k) {
        Map<String, Object> m = attrs.get(e);
        return m != null && m.containsKey(k);
    }

    @Override
    public boolean unset(UUID e, String k) {
        Map<String, Object> m = attrs.get(e);
        if (m == null) {
            return false;
        }
        return m.remove(k) != null;

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
                b.append(" <").append(attr.getKey()).append(',').append(attr.getValue()).append('>');
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
    public Set<UUID> getElements() {
        return attrs.keySet();
    }

    @Override
    public void clear() {
        this.attrs.clear();
    }

    @Override
    public boolean put(UUID e, String k, boolean b) {
        return putObject(e, k, b);
    }

    @Override
    public boolean put(UUID e, String k, String s) {
        return putObject(e, k, s);
    }

    @Override
    public boolean put(UUID e, String k, long l) {
        return putObject(e, k, l);
    }

    @Override
    public boolean put(UUID e, String k, double d) {
        return putObject(e, k, d);
    }

    @Override
    public Boolean getBoolean(UUID e, String k) {
        return (Boolean) get(e, k);
    }

    @Override
    public Long getLong(UUID e, String k) {
        return (Long) get(e, k);
    }

    @Override
    public String getString(UUID e, String k) {
        return (String) get(e, k);
    }

    @Override
    public Double getDouble(UUID e, String k) {
        return (Double) get(e, k);
    }

    @Override
    public Set<String> getKeys(UUID u) {
        Map<String, Object> m = attrs.get(u);
        if (m == null) {
            return Collections.emptySet();
        }
        return m.keySet();
    }

    /**
     * Put a value but try to cast into to a supported primitive if possible.
     * First, it tries to cast {@code v} first to a boolean, then to a long value,
     * finally to a double value. If none of the cast succeeded, the value is let
     * as a string.
     *
     * @param u the element identifier
     * @param k the attribute identifier
     * @param v the value to set
     * @return {@code true} if a previous value was overridden
     */
    public boolean castAndPut(UUID u, String k, String v) {
        String x = v.toLowerCase().trim();
        if (x.equals("true")) {
            return put(u, k, true);
        } else if (x.equals("false")) {
            return put(u, k, false);
        }
        try {
            return put(u, k, Long.parseLong(x));
        } catch (NumberFormatException ex) {
        }

        try {
            return put(u, k, Double.parseDouble(x));
        } catch (NumberFormatException ex) {
        }

        return put(u, k, v);
    }
}
