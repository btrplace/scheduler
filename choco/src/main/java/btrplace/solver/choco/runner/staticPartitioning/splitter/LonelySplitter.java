/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.runner.staticPartitioning.splitter;

import btrplace.model.Instance;
import btrplace.model.Mapping;
import btrplace.model.VM;
import btrplace.model.constraint.Lonely;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Splitter for {@link btrplace.model.constraint.Lonely} constraints.
 * When the constraint focuses VMs among different partitions,
 * the constraint is splitted.
 * <p/>
 * This operation is conservative wrt. the constraint semantic.
 *
 * @author Fabien Hermenier
 */
public class LonelySplitter implements ConstraintSplitter<Lonely> {

    /**
     * Make a new splitter.
     */
    public LonelySplitter() {
        super();
    }

    @Override
    public Class<Lonely> getKey() {
        return Lonely.class;
    }

    @Override
    public boolean split(Lonely cstr, Instance origin, List<Instance> partitions) {
        Set<VM> vms = new HashSet<>(cstr.getInvolvedVMs());
        for (Instance i : partitions) {
            Mapping m = i.getModel().getMapping();
            Set<VM> in = Splitters.extractInside(vms, m.getAllVMs());
            if (!in.isEmpty()) {
                i.getConstraints().add(new Lonely(in, cstr.isContinuous()));
            }
            if (vms.isEmpty()) {
                break;
            }
        }
        return true;
    }
}
