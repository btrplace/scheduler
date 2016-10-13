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

package org.btrplace.safeplace.spec.type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class SetType extends ColType {


    public SetType(Type t) {
        super(t);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SetType setType = (SetType) o;
        if (type == null) {
            return true;
        }
        return type.equals(setType.type);
    }

    @Override
    public String encode() {
        return "set " + type.encode();
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String label() {
        StringBuilder b = new StringBuilder("set<");
        if (type == null) {
            b.append('?');
        } else {
            b.append(type.label());
        }
        return b.append('>').toString();
    }

    @Override
    public String toString() {
        return label();
    }

    public Type enclosingType() {
        return type;
    }

    @Override
    public Object fromJSON(Object c) {
        Set s = new HashSet<>();
        for (Object o : (Collection) c) {
            s.add(type.fromJSON(o));
        }
        return s;
    }
}
