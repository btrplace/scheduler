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

package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Primitive extends Var<Set> {

    private Type type;


    private Primitive(String name, Type enclosingType, Set c) {
        super(name);
        type = new SetType(enclosingType);
    }

    public Primitive(String name, Type enclosingType) {
        this(name, enclosingType, null);
    }

    @Override
    public Set eval(SpecModel mo) {
        Set dom = mo.getVerifDomain(label());
        if (dom == null) {
            throw new UnsupportedOperationException("No domain has been set for primitive '" + label() + "'");
        }
        return dom;
    }

    @Override
    public Type type() {
        return type;
    }
}
