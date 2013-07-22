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
import btrplace.model.constraint.Spread;
import gnu.trove.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.set.hash.THashSet;

import java.util.List;
import java.util.Set;

/**
 * Splitter for {@link Spread} constraints.
 * When the constraint focuses VMs among different partitions,
 * the constraint is splitted.
 * <p/>
 * This operation is conservative wrt. the constraint semantic.
 *
 * @author Fabien Hermenier
 */
public class SpreadSplitter implements ConstraintSplitter<Spread> {

    @Override
    public Class<Spread> getKey() {
        return Spread.class;
    }

    @Override
    public boolean split(final Spread cstr, Instance origin, final List<Instance> partitions, final TIntIntHashMap vmsPosition) {

        TIntObjectHashMap<Set<VM>> map = new TIntObjectHashMap<>((cstr.getInvolvedVMs().size() + 1) / 2);
        for (VM v : cstr.getInvolvedVMs()) {
            int pos = vmsPosition.get(v.id());
            Set<VM> s = map.get(pos);
            if (s == null) {
                s = new THashSet<>(2);
                map.put(pos, s);
            }
            s.add(v);
        }
        map.forEachEntry(new TIntObjectProcedure<Set<VM>>() {
            @Override
            public boolean execute(int a, Set<VM> b) {
                if (b.size() >= 2) {
                    partitions.get(a).getConstraints().add(new Spread(b, cstr.isContinuous()));
                    c++;
                }
                return true;
            }
        });
/*        for (Map.Entry<Integer, Spread> e = map.entr)
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                partitions.get(i).getConstraints().add(new Spread(parts[i], cstr.isContinuous()));
            }
        }*/
        /*for (Instance i : partitions) {
            //Set<VM> in = Splitters.extractVMsIn(vms, i.getModel().getMapping());
            Set<VM> in = Splitters.extractInside(vms, i.getModel().getMapping().getAllVMs());
            if (!in.isEmpty()) {
                i.getConstraints().add(new Spread(in, cstr.isContinuous()));
            }
            if (vms.isEmpty()) {
                break;
            }
        }                                                 */
        return true;
    }

    public static int c = 0;
}
