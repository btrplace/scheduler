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

package org.btrplace.scheduler.choco.runner.disjoint;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.model.*;
import org.btrplace.model.constraint.Among;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Spread;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.DefaultParameters;
import org.testng.Assert;

import java.util.*;

/**
 * Generate models and bench the partitioning algorithm.
 *
 * @author Fabien Hermenier
 */
public class Bench {

    private static Random rnd = new Random();

    private static Set<VM> makeVMSet(Model mo, int nb) {
        Set<VM> s = new HashSet<>(nb);
        for (int i = 0; i < nb; i++) {
            s.add(mo.newVM());
        }
        return s;
    }


    private static List<Node> makeNodeList(Model mo, int nb) {
        List<Node> l = new ArrayList<>(nb);
        for (int i = 0; i < nb; i++) {
            Node n = mo.newNode();
            mo.getMapping().addOnlineNode(n);
            l.add(n);
        }
        return l;
    }

    private static int makeApp(Instance i, int remainder, List<Collection<Node>> edges) {

        int curMax = Math.min(remainder, 30);
        if (remainder <= 30) {
            curMax = remainder;
        } else if (remainder - 30 < 6) {
            curMax = remainder / 2;
        } else {
            curMax = Math.min(curMax, rnd.nextInt(30 - 6) + 6);
        }
        int nbT1 = curMax == 6 ? 2 : rnd.nextInt(curMax - 4 - 2) + 2; //at least 2 VMs, at most max - 4 (2 per tiers)
        curMax -= nbT1;
        int nbT2 = curMax == 4 ? 2 : rnd.nextInt(curMax - 2 - 2) + 2;
        int nbT3 = curMax - nbT2;
        int nbVMs = nbT1 + nbT2 + nbT3;

        Model mo = i.getModel();
        Set<VM> t1 = makeVMSet(mo, nbT1);
        Set<VM> t2 = makeVMSet(mo, nbT2);
        Set<VM> t3 = makeVMSet(mo, nbT3);

        //Make the constraints
        i.getSatConstraints().add(new Spread(t1, true));
        i.getSatConstraints().add(new Spread(t2, true));
        i.getSatConstraints().add(new Spread(t3, true));
        i.getSatConstraints().add(new Among(t3, edges, false));

        //Place the VMs
        //Pick a random edge, and place every VMs on it
        int myEdge = rnd.nextInt(edges.size());
        Collection<Node> nodes = edges.get(myEdge);
        Node n = nodes.iterator().next();
        for (VM v : t1) {
            mo.getMapping().addRunningVM(v, n);
        }
        for (VM v : t2) {
            mo.getMapping().addRunningVM(v, n);
        }
        for (VM v : t3) {
            mo.getMapping().addRunningVM(v, n);
        }

        return nbVMs;
    }

    private static List<Collection<Node>> makeEdges(List<Node> l, int switchSize) {
        TIntIntHashMap parts = new TIntIntHashMap();
        int curPart = 0;
        int i = 0;
        for (Node n : l) {
            i = (i + 1) % switchSize;
            if ((i + 1) % switchSize == 0) {
                curPart++;
            }
            parts.put(n.id(), curPart);
        }
        SplittableElementSet<Node> sp = SplittableElementSet.newNodeIndex(l, parts);
        final List<Collection<Node>> splits = new ArrayList<>();
        sp.forEachPartition(new IterateProcedure<Node>() {
            @Override
            public boolean extract(SplittableElementSet<Node> index, int key, int from, int to) {
                return splits.add(new ElementSubSet<Node>(index, key, from, to));
            }
        });
        return splits;
    }

    public static void benchHA(int nbSamples, Integer partSize, Integer ratio, Integer nbParts) {
        Model mo = new DefaultModel();
        Instance inst = new Instance(mo, new MinMTTR());
        int nbNodes = partSize * nbParts;
        int nbVMs = ratio * nbNodes;

        //Make the infrastructure
        List<Node> l = makeNodeList(mo, nbNodes);
        ShareableResource rcCpu = new ShareableResource("cpu", 20, 0);
        ShareableResource rcMem = new ShareableResource("mem", 16/*GB*/, 0);
        List<Collection<Node>> edges = makeEdges(l, 250);
        mo.attach(rcCpu);
        mo.attach(rcMem);

        while (nbVMs != 0) {
            nbVMs -= makeApp(inst, nbVMs, edges);
        }

        FixedSizePartitioning partitioner = new FixedSizePartitioning(partSize);
        try {
            for (int x = 0; x < nbSamples; x++) {
                long start = System.currentTimeMillis();
                List<Instance> instances = partitioner.split(new DefaultParameters(), inst);
                long end = System.currentTimeMillis();
                System.err.println(instances.size() + " " + nbNodes + " " + nbNodes * ratio + " " + inst.getSatConstraints().size() + " " + (end - start));
            }
        } catch (SchedulerException ex) {
            Assert.fail(ex.getMessage(), ex);
        }
        mo = null;
    }

    public static void main(String[] args) {
        int partSize = 2500;
        int ratio = 6;

        int nbSamples = 100;
        benchHA(nbSamples, partSize, ratio, 1);
        for (int i = 25; i <= 1000; i += 25) {
            benchHA(nbSamples, partSize, ratio, i);
        }
    }
}
