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

package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.spec.type.TimeType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.verification.spec.SpecModel;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * @author Fabien Hermenier
 */
public class Constant extends Term {

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
            StringBuilder b = new StringBuilder("{");
            Iterator ite = ((Collection) o).iterator();
            if (ite.hasNext()) {
                b.append(ite.next().toString());
            }
            while (ite.hasNext()) {
                b.append(", ").append(ite.next());
            }
            b.append('}');
            return b.toString();
        } else if (type().equals(TimeType.getInstance())) {
            return "t" + o;
        }
        return o.toString();
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
    public Object eval(SpecModel mo) {
        return o;
    }
}
