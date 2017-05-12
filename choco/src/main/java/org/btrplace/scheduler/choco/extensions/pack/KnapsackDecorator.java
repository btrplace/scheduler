/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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

package org.btrplace.scheduler.choco.extensions.pack;

import org.btrplace.scheduler.choco.extensions.dancinglist.Cell;
import org.btrplace.scheduler.choco.extensions.dancinglist.DancingList;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

/**
 * An optional extension of VectorPacking allowing knapsack process.
 * - when an item is assigned to a bin and if then assignedLoad == sup(binLoad)
 * for this bin then remove all candidate items from this bin
 * - when an item is assigned to a bin then all the candidate items that are
 * too big to fit the new remaining space are filtered out.
 *
 * @author Sophie Demassey
 */
public class KnapsackDecorator implements KSDecorator {

  /**
   * the core BinPacking propagator
   */
  private VectorPackingPropagator prop;

  /**
   * The list of candidate items for each bin and dimension.
   */
  protected List<List<DancingList<Candidate>>> candidate2;


  private List<Cell>[] cellsPerBin;

  //private static final Comparator<Candidate> dscCandidates = (c1, c2) -> c1.size - c2.size;

  public KnapsackDecorator(VectorPackingPropagator p) {
    this.prop = p;

    candidate2 = new ArrayList<>(p.nbBins);
    for (int b = 0; b < p.nbBins; b++) {
      candidate2 = new ArrayList<>(p.nbDims);
    }

    cellsPerBin = new ArrayList[p.nbBins];
  }

  /**
   * initialize the lists of candidates.
   */
  public void postInitialize() throws ContradictionException {
    List<List<List<Candidate>>> l = new ArrayList<>();
    for (int b = 0; b < prop.nbBins; b++) {
      List<List<Candidate>> inBin = new ArrayList<>(prop.nbDims);
      l.add(inBin);
      for (int d = 0; d < prop.nbDims; d++) {
        List<Candidate> inDim = new ArrayList<>(prop.nbBins);
        inBin.add(inDim);
      }
    }
    for (int i = 0; i < prop.bins.length; i++) {
      IntVar bin = prop.bins[i];
      if (bin.isInstantiated()) {
        continue;
      }

      int[] sizes = new int[prop.nbDims];
      for (int d = 0; d < prop.nbDims; d++) {
        sizes[d] = prop.iSizes[d][i];
      }
      Candidate c = new Candidate(i, sizes);
      int ub = bin.getUB();
      for (int b = bin.getLB(); b <= ub; b = bin.nextValue(b)) {
        for (int d = 0; d < prop.nbDims; d++) {
          l.get(b).get(d).add(c);
        }
      }
    }

    // Sort every list in decreasing order.
    for (int b = 0; b < prop.nbBins; b++) {
      candidate2.add(new ArrayList<>(prop.nbBins));
      for (int d = 0; d < prop.nbDims; d++) {
        List<Candidate> cdts = l.get(b).get(d);
        final int d2 = d;
        cdts.sort((c1, c2) -> c1.sizes[d2] - c2.sizes[d2]);
        candidate2.get(b).add(new DancingList<>(prop.bins[0].getEnvironment(), cdts));
      }
    }

        /*for (int b = 0; b < prop.nbBins; b++) {
            candidate2
        }*/
    fullKnapsack();
  }

  /**
   * Propagate a knapsack on every node and every dimension.
   *
   * @throws ContradictionException
   */
  private void fullKnapsack() throws ContradictionException {
    for (int bin = 0; bin < prop.nbBins; bin++) {
      for (int d = 0; d < prop.nbDims; d++) {
        knapsack(bin, d);
      }
    }
  }

  /**
   * remove all candidate items from a bin that is full
   * then synchronize potentialLoad and sup(binLoad) accordingly
   * if an item becomes instantiated then propagate the newly assigned bin
   *
   * @param bin the full bin
   * @throws ContradictionException
   */
  @SuppressWarnings("squid:S3346")
  private void filterFullDim(int bin, int dim) throws ContradictionException {
    DancingList<Candidate> dl = candidate2.get(bin).get(dim);
    Cell<Candidate> p = dl.head();
    while (p != null) {
      Candidate c = p.content;
      if (c.sizes[dim] == 0) {
        p = dl.next(p);
        continue;
      }
      IntVar var = prop.bins[c.idx];
      if (var.removeValue(bin, prop)) {
        postRemoveItem(c.idx, bin);
        prop.potentialLoad[dim][bin].add(-c.sizes[dim]);
        if (var.isInstantiated()) {
          prop.assignItem(c.idx, var.getValue());
        }
      }
      p = dl.next(p);
    }

    if (dl.head() == null) {
      assert prop.potentialLoad[dim][bin].get() == prop.assignedLoad[dim][bin].get();
      assert prop.loads[dim][bin].getUB() == prop.potentialLoad[dim][bin].get();
    }
  }

  /**
   * update the candidate list of a bin when an item is removed
   *
   * @param item the removed item
   * @param bin  the bin
   */
  @SuppressWarnings("squid:S3346")
  public void postRemoveItem(int item, int bin) {
    //assert candidate.get(bin).get(item);
    //candidate.get(bin).clear(item);
    // TODO: O(1) using a Hash<Int,List<Cell>>
    // TODO: prevent from multiple deletion
    rmCandidate(item, bin);
  }

  private boolean rmCandidate(int item, int bin) {
    for (int d = 0; d < prop.nbDims; d++) {
      DancingList<Candidate> dl = candidate2.get(bin).get(d);
      Cell<Candidate> h = dl.head();
      while (h != null) {
        if (h.content.idx == item) {
          if (!dl.delete(h)) {
            // It is already deleted from the list. It will be the same for
            // the other lists
            return false;
          }
          break;
        }
        h = dl.next(h);
      }
    }
    return true;
  }

  /**
   * update the candidate list of a bin when an item is assigned
   * then apply the full bin filter if sup(binLoad) is reached
   * this function may be recursive
   *
   * @param item the assigned item
   * @param bin  the bin
   * @throws ContradictionException
   */
  public void postAssignItem(int item, int bin) throws ContradictionException {
    // TODO: prevent from the recursion
    if (!rmCandidate(item, bin)) {
      return;
    }
    for (int d = 0; d < prop.nbDims; d++) {
      knapsack(bin, d);

      // The bin is full. We get rid of every candidate and set the bin load.
      if (prop.loads[d][bin].getUB() == prop.assignedLoad[d][bin].get()) {
        DancingList<Candidate> dl = candidate2.get(bin).get(d);
        filterFullDim(bin, d);
        if (dl.size() == 0) {
          for (int d2 = 0; d2 < prop.nbDims; d2++) {
            prop.potentialLoad[d2][bin].set(prop.assignedLoad[d2][bin].get());
            prop.filterLoadSup(d2, bin, prop.potentialLoad[d2][bin].get());
          }
          return;
        }
        return;
      }
    }
  }

  /**
   * Propagate a knapsack on a given dimension and bin.
   * If the usage of an item exceeds the bin free capacity, it is filtered out.
   *
   * @param bin the bin
   * @param d   the dimension
   * @throws ContradictionException
   */
  private void knapsack(int bin, int d) throws ContradictionException {
    final int maxLoad = prop.loads[d][bin].getUB();
    final int free = maxLoad - prop.assignedLoad[d][bin].get();

    DancingList<Candidate> dl = candidate2.get(bin).get(d);

    Cell<Candidate> p = dl.head();
    if (p != null && free >= p.content.sizes[d]) {
      // fail fast. The remaining space > the biggest item.
      return;
    }

    if (free == 0) {
      return;
    }

    // The bin is not full and some items exceeds the remaining space. We
    // get rid of them
    // In parallel, we set the new biggest candidate item for that
    // (bin,dimension)
    while (p != null) {
      if (p.content.sizes[d] <= free) {
        break;
      }
      IntVar var = prop.bins[p.content.idx];
      if (var.removeValue(bin, prop)) {
        prop.removeItem(p.content.idx, bin);

        if (var.isInstantiated()) {
          prop.assignItem(p.content.idx, var.getValue());
        }
      }
      p = dl.next(p);
    }
  }

  public static class Candidate {
    int[] sizes;
    int idx;

    public Candidate(final int idx, final int[] sizes) {
      this.sizes = sizes;
      this.idx = idx;
    }
  }


}
