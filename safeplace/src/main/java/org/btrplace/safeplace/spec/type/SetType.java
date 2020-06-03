/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class SetType extends ColType {


    public SetType(Type t) {
        super(t);
    }

    @Override
    public String collectionLabel() {
        return "set";
    }

    @Override
    public Object fromJSON(Object c) {
        Set s = new HashSet<>();
        for (Object o : (Collection) c) {
            s.add(type.fromJSON(o));
        }
        return s;
    }
}
