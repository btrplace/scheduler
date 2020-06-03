/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ListType extends ColType {

    public ListType(Type t) {
        super(t);
    }

    @Override
    public String collectionLabel() {
        return "list";
    }

    @Override
    public Object fromJSON(Object c) {
        List<Object> s = new ArrayList<>();
        for (Object o : (Collection<?>) c) {
            s.add(type.fromJSON(o));
        }
        return s;
    }

}
