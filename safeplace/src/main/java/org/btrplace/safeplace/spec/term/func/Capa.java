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

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.safeplace.spec.type.IntType;
import org.btrplace.safeplace.spec.type.NodeType;
import org.btrplace.safeplace.spec.type.StringType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.verification.spec.SpecModel;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Capa extends Function<Integer> {

    @Override
    public String id() {
        return "capa";
    }

    @Override
    public Integer eval(SpecModel mo, List<Object> args) {
        throw new UnsupportedOperationException();
        /*String rc = args.get(1).toString();
        ShareableResource r = (ShareableResource) mo.getView(ShareableResource.VIEW_ID_BASE + rc);
        if (r == null) {
            throw new RuntimeException("View '" + ShareableResource.VIEW_ID_BASE + rc + "' is missing");
        }
        return r.getCapacity((Node) args.get(0)); */
    }

    @Override
    public Type[] signature() {
        return new Type[]{NodeType.getInstance(), StringType.getInstance()};
    }


    @Override
    public Type type() {
        return IntType.getInstance();
    }
}
