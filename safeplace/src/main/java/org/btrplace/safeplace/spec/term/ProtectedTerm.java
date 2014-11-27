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

import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.verification.spec.SpecModel;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class ProtectedTerm<T> extends Term<T> {

    private Term<T> t;

    public ProtectedTerm(Term<T> t) {
        this.t = t;
    }

    @Override
    public T eval(SpecModel mo) {
        return t.eval(mo);
    }

    @Override
    public Type type() {
        return t.type();
    }

    @Override
    public UserVar newInclusive(String n, boolean not) {
        return t.newInclusive(n, not);
    }

    @Override
    public UserVar<Set> newPart(String n, boolean not) {
        return t.newPart(n, not);
    }

    @Override
    public String toString() {
        return "(" + t.toString() + ")";
    }
}
