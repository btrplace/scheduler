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

package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.model.VM;
import btrplace.plan.event.Action;
import btrplace.plan.event.VMEvent;
import btrplace.solver.api.cstrSpec.spec.type.ActionType;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.spec.type.VMType;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Get all the actions that manipulate a VM.
 *
 * @author Fabien Hermenier
 */
public class Actions extends Function<Set<Action>> {

    @Override
    public Type type() {
        return new SetType(ActionType.getInstance());
    }


    @Override
    public Set<Action> eval(SpecModel mo, List<Object> args) {
        VM v = (VM) args.get(0);
        if (v == null) {
            throw new UnsupportedOperationException();
        }
        Set<Action> s = new HashSet<>();
        for (Action a : mo.getPlan()) {
            if (a instanceof VMEvent) {
                if (((VMEvent) a).getVM().equals(v)) {
                    s.add(a);
                }
            }
        }
        return s;
    }

    @Override
    public String id() {
        return "actions";
    }

    @Override
    public Type[] signature() {
        return new Type[]{VMType.getInstance()};
    }
}
