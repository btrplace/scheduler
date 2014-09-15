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

import btrplace.model.VM;
import btrplace.safeplace.spec.type.VMType;
import btrplace.safeplace.verification.spec.SpecModel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class AllVMs extends Primitive {

    private static Random rnd = new Random();

    public AllVMs() {
        super("vms", VMType.getInstance());
    }

    @Override
    public Set<VM> eval(SpecModel m) {
        return m.getMapping().VMs();
    }

    @Override
    public VM pickIn(SpecModel mo) {
        int n = rnd.nextInt(mo.getMapping().VMs().size());
        Iterator<VM> it = mo.getMapping().VMs().iterator();

        while (n > 0) {
            it.next();
            n--;
        }
        VM v = it.next();
        return v;
    }

    @Override
    public Set<VM> pickIncluded(SpecModel mo) {
        Set<VM> s = new HashSet<>();
        for (VM v : mo.getMapping().VMs()) {
            if (rnd.nextBoolean()) {
                s.add(v);
            }
        }
        return s;
    }

    @Override
    public boolean isConstant() {
        return true;
    }
}
