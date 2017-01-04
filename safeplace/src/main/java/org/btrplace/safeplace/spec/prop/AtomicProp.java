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

package org.btrplace.safeplace.spec.prop;

import org.btrplace.safeplace.spec.term.Term;

/**
 * @author Fabien Hermenier
 */
public abstract class AtomicProp implements Proposition {

    protected Term a;
    protected Term b;

    private String op;

    public AtomicProp(Term a, Term b, String op) {
        this.a = a;
        this.b = b;
        this.op = op;
    }

    @Override
    public String toString() {
        return a.toString() + " " + op + " " + b.toString();
    }
}
