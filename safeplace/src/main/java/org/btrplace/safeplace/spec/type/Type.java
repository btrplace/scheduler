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

package org.btrplace.safeplace.spec.type;

/**
 * @author Fabien Hermenier
 */
public interface Type {

    default String label() {
        return toString();
    }

    default Object toJSON(Object value) {
        throw new UnsupportedOperationException();
    }

    default String encode() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName());
    }

    static Type decode(String str) {
        String[] toks = str.split(" ");
        switch(toks[0]) {
            case "int": return IntType.getInstance();
            case "node": return NodeType.getInstance();
            case "vm": return VMType.getInstance();
            case "string": return StringType.getInstance();
            case "list": return new ListType(decode(str.substring(str.indexOf(' ') + 1)));
            case "set": return new SetType(decode(str.substring(str.indexOf(' ') + 1)));
            default:
                throw new IllegalArgumentException("Unsupported " + str);
        }

    }

    default Object fromJSON(Object value) {
        throw new UnsupportedOperationException("Unsupported:"  + value.toString());
    }
}
