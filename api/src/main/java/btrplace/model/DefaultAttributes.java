/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

    private Map<VM, Map<String, Object>> vmAttrs;
    private Map<Node, Map<String, Object>> nodeAttrs;

    /**
     * Make a new empty list of attributes.
     */
    public DefaultAttributes() {
        vmAttrs = new HashMap<>();
        nodeAttrs = new HashMap<>();
    }

    private boolean putObject(VM e, String k, Object v) {
        Map<String, Object> m = vmAttrs.get(e);
        if (m == null) {
            m = new HashMap<>();
            vmAttrs.put(e, m);
        }
        return m.put(k, v) != null;
    }

    private boolean putObject(Node e, String k, Object v) {
        Map<String, Object> m = nodeAttrs.get(e);
        if (m == null) {
            m = new HashMap<>();
            nodeAttrs.put(e, m);
        }
        return m.put(k, v) != null;
    }

    @Override
    public Object get(VM e, String k) {
        Map<String, Object> m = vmAttrs.get(e);
        if (m == null) {
            return null;
        }
        return m.get(k);
    }

    @Override
    public Object get(Node e, String k) {
        Map<String, Object> m = nodeAttrs.get(e);
        if (m == null) {
            return null;
        }
        return m.get(k);
    }

    @Override
    public boolean isSet(VM e, String k) {
        Map<String, Object> m = vmAttrs.get(e);
        return m != null && m.containsKey(k);
    }

    @Override
    public boolean isSet(Node e, String k) {
        Map<String, Object> m = nodeAttrs.get(e);
        return m != null && m.containsKey(k);
    }

    @Override
    public boolean unset(VM e, String k) {
        Map<String, Object> m = vmAttrs.get(e);
        if (m == null) {
            return false;
        }
        return m.remove(k) != null;
    }

    @Override
    public boolean unset(Node e, String k) {
        Map<String, Object> m = nodeAttrs.get(e);
        if (m == null) {
            return false;
        }
        return m.remove(k) != null;
    }

    @Override
    public Attributes clone() {
        DefaultAttributes cpy = new DefaultAttributes();
        for (Map.Entry<VM, Map<String, Object>> e : vmAttrs.entrySet()) {
            cpy.vmAttrs.put(e.getKey(), new HashMap<>(e.getValue()));
        }

        for (Map.Entry<Node, Map<String, Object>> e : nodeAttrs.entrySet()) {
            cpy.nodeAttrs.put(e.getKey(), new HashMap<>(e.getValue()));
        }

        return cpy;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<VM, Map<String, Object>> e : vmAttrs.entrySet()) {
            b.append(e.getKey());
            b.append(':');
            b.append(stringify(e.getValue()));
            b.append('\n');
        }
        for (Map.Entry<Node, Map<String, Object>> e : nodeAttrs.entrySet()) {
            b.append(e.getKey());
            b.append(':');
            b.append(stringify(e.getValue()));
            b.append('\n');
        }
        return b.toString();
    }

    private String stringify(Map<String, Object> map) {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, Object> attr : map.entrySet()) {
            b.append(" <").append(attr.getKey()).append(',');
            Object val = attr.getValue();
            if (val instanceof String) {
                b.append('"').append(val).append('"');
            } else {
                b.append(val);
            }
            b.append('>');
        }
        return b.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(vmAttrs, nodeAttrs);
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
        return vmAttrs.equals(that.vmAttrs) && nodeAttrs.equals(that.nodeAttrs);
    }

    @Override
    public Set<VM> getSpecifiedVMs() {
        return vmAttrs.keySet();
    }

    @Override
    public Set<Node> getSpecifiedNodes() {
        return nodeAttrs.keySet();
    }

    @Override
    public void clear() {
        this.vmAttrs.clear();
        this.nodeAttrs.clear();
    }

    @Override
    public boolean put(VM e, String k, boolean b) {
        return putObject(e, k, b);
    }

    @Override
    public boolean put(Node e, String k, boolean b) {
        return putObject(e, k, b);
    }

    @Override
    public boolean put(VM e, String k, int n) {
        return putObject(e, k, n);
    }

    @Override
    public boolean put(Node e, String k, int n) {
        return putObject(e, k, n);
    }

    @Override
    public boolean put(VM e, String k, String s) {
        return putObject(e, k, s);
    }

    @Override
    public boolean put(Node e, String k, String s) {
        return putObject(e, k, s);
    }

    @Override
    public boolean put(VM e, String k, double d) {
        return putObject(e, k, d);
    }

    @Override
    public boolean put(Node e, String k, double d) {
        return putObject(e, k, d);
    }

    @Override
    public Boolean getBoolean(VM e, String k) {
        return (Boolean) get(e, k);
    }

    @Override
    public Boolean getBoolean(Node e, String k) {
        return (Boolean) get(e, k);
    }

    @Override
    public String getString(VM e, String k) {
        Object o = get(e, k);
        return o == null ? null : o.toString();
    }

    @Override
    public String getString(Node e, String k) {
        Object o = get(e, k);
        return o == null ? null : o.toString();
    }

    @Override
    public Double getDouble(VM e, String k) {
        return (Double) get(e, k);
    }

    @Override
    public Double getDouble(Node e, String k) {
        return (Double) get(e, k);
    }

    @Override
    public Integer getInteger(VM e, String k) {
        return (Integer) get(e, k);
    }

    @Override
    public Integer getInteger(Node e, String k) {
        return (Integer) get(e, k);
    }


    @Override
    public Set<String> getKeys(VM e) {
        Map<String, Object> m = vmAttrs.get(e);
        if (m == null) {
            return Collections.emptySet();
        }
        return m.keySet();
    }

    @Override
    public Set<String> getKeys(Node e) {
        Map<String, Object> m = nodeAttrs.get(e);
        if (m == null) {
            return Collections.emptySet();
        }
        return m.keySet();
    }


    @Override
    public boolean castAndPut(VM u, String k, String v) {
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

    @Override
    public boolean castAndPut(Node u, String k, String v) {
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
