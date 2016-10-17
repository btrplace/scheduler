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

package org.btrplace.safeplace.spec.term;

import net.minidev.json.JSONObject;
import org.btrplace.safeplace.spec.type.*;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class Constant implements Term {

    private Type t;

    private Object o;

    public Constant(Object o, Type t) {
        this.t = t;
        this.o = o;
    }

    public Type type() {
        return t;
    }

    @Override
    public String toString() {
        if (o instanceof Collection) {
            return ((Collection) o).stream().map(Object::toString).collect(Collectors.joining(", ","{","}")).toString();
        }
        return o.toString();
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.put("type", type().encode());
        o.put("value", type().toJSON(eval(null)));
        return o;
    }

    public static Constant fromJSON(JSONObject o) {
        Type t = Type.decode(o.getAsString("type"));
        Object r = t.fromJSON(o.get("value"));
        return new Constant(r, t);
    }

    private static Type type(JSONObject o) {
        switch(o.getAsString("type")) {
            case "int":
                return IntType.getInstance();
            case "set":
                return new SetType(type((JSONObject) o.get("value")));
            case "list":
                return new ListType(type((JSONObject) o.get("value")));
            case "string":
                return StringType.getInstance();
        }
        throw new IllegalArgumentException(o.getAsString("type"));
    }

    @Override
    public boolean equals(Object o1) {
        if (this == o1) return true;
        if (!(o1 instanceof Constant)) return false;

        Constant value = (Constant) o1;

        return (o.equals(value.o) && t.equals(value.t));
    }

    @Override
    public int hashCode() {
        return Objects.hash(t, o);
    }

    @Override
    public Object eval(Context mo, Object... terms) {
        return o;
    }
}
