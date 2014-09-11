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

package btrplace.safeplace.spec.term;

import btrplace.safeplace.spec.type.NoneType;
import btrplace.safeplace.spec.type.Type;
import btrplace.safeplace.verification.spec.SpecModel;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class None extends Primitive {

    private static None instance = new None();

    public None() {
        super(null, NoneType.getInstance());
    }

    @Override
    public Set eval(SpecModel m) {
        return null;
    }

    public static None instance() {
        return instance;
    }

    @Override
    public UserVar newInclusive(String n, boolean not) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserVar<Set> newPart(String n, boolean not) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type type() {
        return NoneType.getInstance();
    }

    @Override
    public String label() {
        return type().label();
    }
}
