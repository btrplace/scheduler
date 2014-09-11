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

package btrplace.safeplace.spec.term.func;

import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.safeplace.spec.type.NodeType;
import btrplace.safeplace.spec.type.SetType;
import btrplace.safeplace.spec.type.Type;
import btrplace.safeplace.spec.type.VMType;
import btrplace.safeplace.verification.spec.SpecModel;

import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Hosted extends Function<Set<VM>> {

    @Override
    public SetType type() {
        return new SetType(VMType.getInstance());
    }

    @Override
    public Set<VM> eval(SpecModel mo, List<Object> args) {
        Node n = (Node) args.get(0);
        if (n == null) {
            throw new UnsupportedOperationException();
        }
        return mo.getMapping().hosted(n);
    }

    @Override
    public String id() {
        return "hosted";
    }

    @Override
    public Type[] signature() {
        return new Type[]{NodeType.getInstance()};
    }
}
