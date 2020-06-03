/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.safeplace.spec.type.IntType;
import org.btrplace.safeplace.spec.type.ListType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Get all the indexes of a given list.
 *
 * @author Fabien Hermenier
 */
public class Range implements Function<List<Integer>> {

    @Override
    public Type type() {
        return new ListType(IntType.getInstance());
    }


    @Override
    public List<Integer> eval(Context mo, Object... args) {
        List c = (List) args[0];
        if (c == null) {
            return null;
        }
        List<Integer> res = new ArrayList<>(c.size());
        for (int i = 0; i < c.size(); i++) {
            res.add(i);
        }
        return res;
    }

    @Override
    public String id() {
        return "range";
    }

    @Override
    public Type[] signature() {
        return new Type[]{new ListType(null)};
    }
}
