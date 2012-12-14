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
 * Denotes an integer resource value for an element.
 *
 * @author Fabien Hermenier
 */
public class DefaultShareableResource implements ShareableResource, Cloneable {

    private Map<UUID, Integer> values;

    private int noValue;

    private String id;

    public static final int DEFAULT_NO_VALUE = 0;

    /**
     * Make a new resource that use {@link #DEFAULT_NO_VALUE} as value to denote an undefined value.
     *
     * @param id the resource identifier
     */
    public DefaultShareableResource(String id) {
        this(id, DEFAULT_NO_VALUE);
    }

    /**
     * Make a new resource.
     *
     * @param id      the resource identifier
     * @param noValue the value to use to denote an undefined value
     */
    public DefaultShareableResource(String id, int noValue) {
        values = new HashMap<UUID, Integer>();
        this.id = id;
        this.noValue = noValue;
    }

    @Override
    public int get(UUID n) {
        if (values.containsKey(n)) {
            return values.get(n);
        }
        return noValue;
    }

    @Override
    public List<Integer> get(List<UUID> ids) {
        List<Integer> res = new ArrayList<Integer>(ids.size());
        for (UUID u : ids) {
            res.add(get(u));
        }
        return res;
    }

    @Override
    public Set<UUID> getDefined() {
        return values.keySet();
    }

    @Override
    public ShareableResource set(UUID n, int val) {
        values.put(n, val);
        return this;
    }

    @Override
    public boolean unset(UUID n) {
        return values.remove(n) != null;
    }

    @Override
    public boolean defined(UUID n) {
        return values.containsKey(n);
    }

    @Override
    public int compare(UUID o1, UUID o2) {
        return get(o1) - get(o2);
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public int max(Collection<UUID> ids, boolean undef) {
        int m = Integer.MIN_VALUE;
        for (UUID u : ids) {
            if (defined(u) || undef) {
                int x = defined(u) ? values.get(u) : noValue;
                if (x > m) {
                    m = x;
                }
            }
        }
        return m;
    }

    @Override
    public int min(Collection<UUID> ids, boolean undef) {
        int m = Integer.MAX_VALUE;
        for (UUID u : ids) {
            if (defined(u) || undef) {
                int x = defined(u) ? values.get(u) : noValue;
                if (x < m) {
                    m = x;
                }
            }
        }
        return m;
    }

    @Override
    public int sum(Collection<UUID> ids, boolean undef) {
        int s = 0;
        for (UUID u : ids) {
            if (defined(u) || undef) {
                s += defined(u) ? values.get(u) : noValue;
            }
        }
        return s;
    }

    @Override
    public int getDefaultValue() {
        return noValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ShareableResource that = (ShareableResource) o;

        if (!that.getDefined().equals(values.keySet())) {
            return false;
        }

        for (UUID k : values.keySet()) {
            if (!values.get(k).equals(that.get(k))) {
                return false;
            }
        }
        return id.equals(that.getIdentifier());
    }

    @Override
    public int hashCode() {
        return id.hashCode() + 31 * values.hashCode();
    }

    @Override
    public ShareableResource clone() {
        DefaultShareableResource rc = new DefaultShareableResource(id, noValue);
        for (Map.Entry<UUID, Integer> e : values.entrySet()) {
            rc.values.put(e.getKey(), e.getValue());
        }
        return rc;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("rc:").append(id).append(":");
        for (Iterator<Map.Entry<UUID, Integer>> ite = values.entrySet().iterator(); ite.hasNext(); ) {
            Map.Entry<UUID, Integer> e = ite.next();
            buf.append('<').append(e.getKey().toString()).append(',').append(e.getValue()).append('>');
            if (ite.hasNext()) {
                buf.append(',');
            }
        }
        return buf.toString();

    }
}
