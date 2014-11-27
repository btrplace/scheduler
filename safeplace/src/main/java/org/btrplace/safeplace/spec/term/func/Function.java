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

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.verification.spec.SpecModel;

import java.util.Collection;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public abstract class Function<T> {

    public abstract String id();

    public abstract Type type();

    public Type type(List<Term> args) {
        return type();
    }

    public abstract Type[] signature();

    public Type[] signature(List<Term> args) {
        return signature();
    }

    public abstract T eval(SpecModel mo, List<Object> args);

    public Object pickIn(SpecModel mo, List<Term> args) {
        throw new UnsupportedOperationException("Sth in " + this.type() + " " + toString() + "(" + args + ")");
    }

    public Object pickIncluded(SpecModel mo, List<Term> args) {
        throw new UnsupportedOperationException("Sth included " + this.type() + " " + toString() + "(" + args + ")");
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(id()).append('(');
        Type[] expected = signature();
        for (int i = 0; i < expected.length; i++) {
            b.append(expected[i]);
            if (i < expected.length - 1) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

    public boolean contains(SpecModel mo, List<Object> args, Object o) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not support contains()");
    }

    public boolean containsAll(SpecModel mo, List<Object> args, Collection<Object> col) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not support contains()");
    }
}
