/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.type;

/**
 * @author Fabien Hermenier
 */
public interface Type {

    default String label() {
        return toString();
    }

    default Object toJSON(@SuppressWarnings("unused") Object value) {
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
