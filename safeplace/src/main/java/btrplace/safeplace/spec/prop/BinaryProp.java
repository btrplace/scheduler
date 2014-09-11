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

package btrplace.safeplace.spec.prop;

/**
 * A sequence of propositions having a same operator.
 *
 * @author Fabien Hermenier
 */
public abstract class BinaryProp implements Proposition {

    protected Proposition p1, p2;

    public BinaryProp(Proposition p1, Proposition p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public String toString() {
        return p1.toString() + " " + operator() + " " + p2.toString();
    }

    public abstract String operator();

    public Proposition first() {
        return p1;
    }

    public Proposition second() {
        return p2;
    }
}
