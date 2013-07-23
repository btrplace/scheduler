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

package btrplace.solver.choco.runner.staticPartitioning;

import btrplace.model.*;
import btrplace.model.constraint.MinMTTR;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.Spread;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithmParams;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithParams;
import gnu.trove.set.hash.THashSet;
import org.testng.Assert;
import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Bench {

    @DataProvider
    public Object[][] getPartData() {
        int nbNodes = 1000000;
        int partSize = 2500;
        return new Integer[][]
                {
                       /* {nbNodes, 5, partSize},
                        {nbNodes, 6, partSize},
                        {nbNodes, 7, partSize},
                        {nbNodes, 8, partSize},
                        {nbNodes, 9, partSize},       */
                        {nbNodes, 10, partSize}
                };
    }

    //@Test(dataProvider = "getPartData")
    public void simpleBench(Integer nbNodes, Integer ratio, Integer partSize) throws SolverException {

        List<Node> nodes = new ArrayList<>();
        Model mo = new DefaultModel();
        for (int i = 0; i < nbNodes; i++) {
            Node n = mo.newNode();
            nodes.add(n);
            mo.getMapping().addOnlineNode(n);
        }

        //Random VM Placement
        int nbVMs = nbNodes * ratio;
        Random rnd = new Random();
        for (int i = 0; i < nbVMs; i++) {
            VM v = mo.newVM();
            Node n = nodes.get(rnd.nextInt(nbNodes));
            mo.getMapping().addRunningVM(v, n);
        }

        //Bunch of spread constraints
        List<SatConstraint> cstrs = new ArrayList<>();
        Set<VM> s = new THashSet<>();
        for (Node n : mo.getMapping().getOnlineNodes()) {
            for (VM v : mo.getMapping().getRunningVMs(n)) {
                s.add(v);
                if (rnd.nextInt(6) == 0 && s.size() > 1) {
                    cstrs.add(new Spread(s, true));
                    s = new THashSet<>();
                }
            }
        }
        StaticPartitioning partitioner = new FixedSizePartitioning(partSize);
        ChocoReconfigurationAlgorithmParams ps = new DefaultChocoReconfigurationAlgorithParams();
        long st = System.currentTimeMillis();
        List<Instance> parts = partitioner.split(ps, new Instance(mo, cstrs, new MinMTTR()));
        System.err.println(nbNodes + " nodes; " + nbVMs + " vms; " + parts.size() + "x" + partSize + " nodes; " + cstrs.size() + " constraints; " + (System.currentTimeMillis() - st) + "ms");
        Assert.assertEquals(parts.size(), nbNodes / partSize);
        Assert.fail();
    }
}
