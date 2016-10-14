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

import org.btrplace.safeplace.spec.type.SetType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.Domain;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Primitive<T> implements Var<Set<T>> {

    private Type type;

    private String lbl;

    private Set<T> cache = null;

    public Primitive(String name, Type enclosingType) {
        lbl = name;
        type = new SetType(enclosingType);
    }

    @Override
    public Set<T> eval(Context mo, Object... args) {
        Domain<T> dom = mo.domain(label());
        if (dom.constant()) {

            if (cache == null) {
                cache = new HashSet<>(dom.values());
            }
            return cache;
        }
        return new HashSet<>(dom.values());
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public String label() {
        return lbl;
    }

    @Override
    public String pretty() {
        return label() + ":" + type();
    }

    @Override
    public String toString() {
        return label();
    }
}
