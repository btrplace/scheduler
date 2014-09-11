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

package btrplace.safeplace.spec.term;

import btrplace.safeplace.spec.type.Type;

/**
 * @author Fabien Hermenier
 */
public abstract class Plus<T> extends Term<T> {

    protected Term<T> a, b;

    public Plus(Term<T> t1, Term<T> t2) {
        this.a = t1;
        this.b = t2;
    }

    @Override
    public String toString() {
        return a.toString() + " + " + b.toString();
    }

    @Override
    public Type type() {
        return a.type();
    }
}
