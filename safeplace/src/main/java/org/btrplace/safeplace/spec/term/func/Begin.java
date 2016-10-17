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

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.plan.event.Action;
import org.btrplace.safeplace.spec.type.ActionType;
import org.btrplace.safeplace.spec.type.IntType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

/**
 * Get the moment an action begins.
 *
 * @author Fabien Hermenier
 */
public class Begin implements Function<Integer> {

    @Override
    public IntType type() {
        return IntType.getInstance();
    }

    @Override
    public Integer eval(Context mo, Object... args) {
        Action a = (Action) args[0];
        if (a == null) {
            throw new UnsupportedOperationException();
        }
        return a.getStart();
    }

    @Override
    public String id() {
        return "begin";
    }

    @Override
    public Type[] signature() {
        return new Type[]{ActionType.getInstance()};
    }
}
