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

package org.btrplace.safeplace.spec.type;

/**
 * @author Fabien Hermenier
 */
public class ListType extends ColType {

    public ListType(Type t) {
        super(t);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListType setType = (ListType) o;
        if (type == null) {
            return true;
        }
        return type.equals(setType.type);
    }

    @Override
    public String label() {
        StringBuilder b = new StringBuilder("list<");
        if (type == null) {
            b.append('?');
        } else {
            b.append(type.label());
        }
        return b.append('>').toString();
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public Type inside() {
        return type;
    }

    public Type enclosingType() {
        return type;
    }

    @Override
    public boolean comparable(Type t) {
        return t.equals(NoneType.getInstance()) || equals(t);
    }
}
