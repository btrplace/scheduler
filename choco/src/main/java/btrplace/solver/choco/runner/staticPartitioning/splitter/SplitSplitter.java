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
import btrplace.model.VM;
import btrplace.model.constraint.Split;

import java.util.*;

/**
 * Splitter for {@link btrplace.model.constraint.Split} constraints.
 * When the constraint focuses VMs among different partitions,
 * the constraint is splitted.
 * <p/>
 * This operation is conservative wrt. the constraint semantic.
 *
 * @author Fabien Hermenier
 */
public class SplitSplitter implements ConstraintSplitter<Split> {

    @Override
    public Class<Split> getKey() {
        return Split.class;
    }

    @Override
    public boolean split(Split cstr, List<Instance> partitions) {
        List<Set<VM>> vms = new ArrayList<>();
        for (Collection<VM> s : cstr.getSets()) {
            vms.add(new HashSet<>(s));
        }
        for (Instance i : partitions) {
            Collection<Collection<VM>> subSplit = new ArrayList<>();
            for (Set<VM> s : vms) {
                Set<VM> in = Splitters.extractInside(s, i.getModel().getVMs());
                if (!in.isEmpty()) {
                    subSplit.add(in);
                }
            }
            if (!subSplit.isEmpty()) {
                i.getConstraints().add(new Split(subSplit, cstr.isContinuous()));
            }
            boolean allEmpties = true;
            for (Set<VM> s : vms) {
                if (!s.isEmpty()) {
                    allEmpties = false;
                    break;
                }
            }
            if (allEmpties) {
                break;
            }
        }
        return true;
    }
}
