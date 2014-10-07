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

package org.btrplace.model;

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

    private boolean putObject(Element e, String k, Object v) {
        Map<String, Object> m;
        if (e instanceof VM) {
            m = vmAttrs.get(e);
            if (m == null) {
                m = new HashMap<>();
                vmAttrs.put((VM) e, m);
            }
        } else if (e instanceof Node) {
            m = nodeAttrs.get(e);
            if (m == null) {
                m = new HashMap<>();
                nodeAttrs.put((Node) e, m);
            }
        } else {
            return false;
        }
        return m.put(k, v) != null;
    }

    @Override
    public Object get(Element e, String k) {
        Map<String, Object> m;
        if (e instanceof Node) {
            m = nodeAttrs.get(e);
        } else if (e instanceof VM) {
            m = vmAttrs.get(e);
        } else {
            return null;
        }
        return m == null ? null : m.get(k);
    }

    @Override
    public boolean isSet(Element e, String k) {
        Map<String, Object> m;
        if (e instanceof Node) {
            m = nodeAttrs.get(e);
        } else if (e instanceof VM) {
            m = vmAttrs.get(e);
        } else {
            return false;
        }
        return m != null && m.containsKey(k);
    }

    @Override
    public boolean unset(Element e, String k) {
        Map<String, Object> m;
        if (e instanceof Node) {
            m = nodeAttrs.get(e);
        } else if (e instanceof VM) {
            m = vmAttrs.get(e);
        } else {
            return false;
        }
        return m != null && m.remove(k) != null;
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
    public Set<Element> getDefined() {
        Set<Element> s = new HashSet<>(vmAttrs.size() + nodeAttrs.size());
        s.addAll(vmAttrs.keySet());
        s.addAll(nodeAttrs.keySet());
        return s;
    }

    @Override
    public void clear() {
        this.vmAttrs.clear();
        this.nodeAttrs.clear();
    }

    @Override
    public boolean put(Element e, String k, boolean b) {
        return putObject(e, k, b);
    }

    @Override
    public boolean put(Element e, String k, int n) {
        return putObject(e, k, n);
    }

    @Override
    public boolean put(Element e, String k, String s) {
        return putObject(e, k, s);
    }

    @Override
    public boolean put(Element e, String k, double d) {
        return putObject(e, k, d);
    }

    @Override
    public Boolean getBoolean(Element e, String k) {
        return (Boolean) get(e, k);
    }

    @Override
    public String getString(Element e, String k) {
        Object o = get(e, k);
        return o == null ? null : o.toString();
    }

    @Override
    public Double getDouble(Element e, String k) {
        return (Double) get(e, k);
    }

    @Override
    public Integer getInteger(Element e, String k) {
        return (Integer) get(e, k);
    }

    @Override
    public Set<String> getKeys(Element e) {
        Map<String, Object> m;
        if (e instanceof Node) {
            m = nodeAttrs.get(e);
        } else if (e instanceof VM) {
            m = vmAttrs.get(e);
        } else {
            return Collections.emptySet();
        }
        return m == null ? Collections.<String>emptySet() : m.keySet();
    }

    @Override
    public boolean castAndPut(Element e, String k, String v) {
        String x = v.toLowerCase().trim();
        if (x.equals("true")) {
            return put(e, k, true);
        } else if (x.equals("false")) {
            return put(e, k, false);
        }
        try {
            return put(e, k, Integer.parseInt(x));
        } catch (NumberFormatException ignored) {
        }

        try {
            return put(e, k, Double.parseDouble(x));
        } catch (NumberFormatException ignored) {
        }

        return put(e, k, v);
    }

    @Override
    public void clear(Element e) {
        if (e instanceof VM) {
            this.vmAttrs.remove(e);
        } else if (e instanceof Node) {
            this.nodeAttrs.remove(e);
        }
    }
}
