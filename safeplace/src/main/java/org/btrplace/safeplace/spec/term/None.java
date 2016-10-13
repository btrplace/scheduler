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

package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.spec.type.NoneType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class None extends Primitive {

    private static None instance = new None();

    public None() {
        super("none", NoneType.getInstance());
    }

    @Override
    public Set eval(Context m, Object... args) {
        return null;
    }

    public static None instance() {
        return instance;
    }

    @Override
    public Type type() {
        return NoneType.getInstance();
    }

    @Override
    public String label() {
        return type().label();
    }

    @Override
    public String pretty() {
        return "none";
    }
}
