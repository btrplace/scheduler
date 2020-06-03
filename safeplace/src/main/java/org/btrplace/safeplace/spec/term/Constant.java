/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term;

import net.minidev.json.JSONObject;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class Constant implements Term {

  private final Type t;

  private final Object o;

    public Constant(Object o, Type t) {
        this.t = t;
        this.o = o;
    }

    @Override
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
        JSONObject ob = new JSONObject();
        ob.put("type", type().encode());
        ob.put("value", type().toJSON(eval(null)));
        return ob;
    }

    public static Constant fromJSON(JSONObject o) {
        Type t = Type.decode(o.getAsString("type"));
        Object r = t.fromJSON(o.get("value"));
        return new Constant(r, t);
    }

    @Override
    public boolean equals(Object o1) {
        if (this == o1) {
            return true;
        }
        if (!(o1 instanceof Constant)) {
            return false;
        }

        Constant value = (Constant) o1;

        return o.equals(value.o) && t.equals(value.t);
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
