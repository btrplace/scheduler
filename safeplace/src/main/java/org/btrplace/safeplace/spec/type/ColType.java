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

import net.minidev.json.JSONArray;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Fabien Hermenier
 */
public class ColType implements Type {

    protected Type type;

    public ColType(Type t) {
        type = t;
    }

    public String collectionLabel() {
        return "col";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ColType)) {
            return false;
        }
        if (type == null) {
            return true;
        }
        ColType colType = (ColType) o;
        return type.equals(colType.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return collectionLabel() + "<" + (type == null ? "?" : type.label()) + ">";
    }

    public Type inside() {
        return type;
    }

    public Type enclosingType() {
        return type;
    }

    @Override
    public String encode() {
        return collectionLabel() + " " + type.encode();
    }

    @Override
    public JSONArray toJSON(Object c) {
        JSONArray a = new JSONArray();
        //return ((VM)value).id();
        for (Object o : (Collection) c) {
            a.add(type.toJSON(o));
        }
        return a;
    }

}
