/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
        for (Object o : (Collection) c) {
            a.add(type.toJSON(o));
        }
        return a;
    }

}
