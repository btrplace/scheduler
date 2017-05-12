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

import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.memory.structure.S64BitSet;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.stream.IntStream;

/**
 * An optional extension of VectorPacking allowing knapsack process.
 * - when an item is assigned to a bin and if then assignedLoad == sup(binLoad)
 * for this bin then remove all candidate items from this bin
 * - when an item is assigned to a bin then all the candidate items that are
 * too big to fit the new remaining space are filtered out.
 *
 * @author Sophie Demassey
 */
public class KnapsackDecorator2 implements KSDecorator {

  /**
   * the core BinPacking propagator
   */
  private VectorPackingPropagator prop;

  /**
   * Item indexes sorted dsc per dimension.
   * sortedItems[d][x] = i <-> item i is the Xth biggest in dimension d.
   */
  private int[][] sortedItems;

  /**
   * Reverse map. revSortedItems[d][i] = x <-> item i is the Xth biggest in dimension d;
   */
  private int[][] revSortedItems;

  /**
   * Candidates per node and dimension
   * candidate[n][d].get(x) <-> item at position x in the sorted dimension[d]
   */
  private IStateBitSet[][] candidates;

  public KnapsackDecorator2(VectorPackingPropagator p) {
    this.prop = p;

    int[] items = new int[prop.bins.length];
    for (int i = 0; i < prop.bins.length; i++) {
      items[i] = i;
    }
    // Sort the items on every dimension.
    sortedItems = new int[prop.nbDims][items.length];
    revSortedItems = new int[prop.nbDims][];
    for (int d = 0; d < prop.nbDims; d++) {
      final int d2 = d;
      sortedItems[d] = IntStream.of(items).boxed()
              .sorted((a, b) -> prop.iSizes[d2][b] - prop.iSizes[d2][a])
              .mapToInt(i -> i).toArray();
      revSortedItems[d] = new int[items.length];
      for (int x = 0; x < items.length; x++) {
        revSortedItems[d][sortedItems[d][x]] = x;
      }
    }

    // The bitsets.
    candidates = new IStateBitSet[prop.nbBins][prop.nbDims];
    for (int b = 0; b < prop.nbBins; b++) {
      for (int d = 0; d < prop.nbDims; d++) {
        candidates[b][d] = new S64BitSet(p.getModel().getEnvironment(), p.bins.length);
      }
    }
  }

  /**
   * initialize the lists of candidates.
   */
  public void postInitialize() throws ContradictionException {

    // Create all the candidate bitsets
    for (int i = 0; i < prop.bins.length; i++) {
      IntVar bin = prop.bins[i];
      if (bin.isInstantiated()) {
        continue;
      }

      int ub = bin.getUB();
      for (int b = bin.getLB(); b <= ub; b = bin.nextValue(b)) {
        for (int d = 0; d < prop.nbDims; d++) {
          // The position of the item in the sorted item array
          int x = revSortedItems[d][i];
          candidates[b][d].set(x);
        }
      }
    }
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
    IStateBitSet bs = candidates[bin][dim];

    for (int x = bs.nextSetBit(0); x >= 0; x = bs.nextSetBit(x + 1)) {
      int i = revSortedItems[dim][x];
      if (prop.iSizes[dim][i] == 0) {
        // Sorted dsc so nothing interesting here.
        break;
      }

      IntVar var = prop.bins[i];
      if (var.removeValue(bin, prop)) {
        //postRemoveItem(i, bin);
        prop.potentialLoad[dim][bin].add(-prop.iSizes[dim][i]);
        if (var.isInstantiated()) {
          prop.assignItem(i, var.getValue());
        }
      }
    }
    if (bs.isEmpty()) {
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
    rmCandidate(item, bin);
  }

  private boolean rmCandidate(int item, int bin) {
    for (int d = 0; d < prop.nbDims; d++) {
      int x = revSortedItems[d][item];
      candidates[bin][d].clear(x);
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
    int x = revSortedItems[0][item];
    if (!candidates[bin][0].get(x)) {
      return;
    }
    rmCandidate(item, bin);
    for (int d = 0; d < prop.nbDims; d++) {
      knapsack(bin, d);

      // The bin is full. We get rid of every candidate and set the bin load.
      if (prop.loads[d][bin].getUB() == prop.assignedLoad[d][bin].get()) {
        filterFullDim(bin, d);
        if (candidates[bin][0].isEmpty()) {
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

    IStateBitSet bs = candidates[bin][d];
    for (int x = bs.nextSetBit(0); x >= 0; x = bs.nextSetBit(x + 1)) {
      int i = sortedItems[d][x];
      if (prop.iSizes[d][i] <= free) {
        break;
      }
      IntVar var = prop.bins[i];
      if (var.removeValue(bin, prop)) {
        prop.removeItem(i, bin);

        if (var.isInstantiated()) {
          prop.assignItem(i, var.getValue());
        }
      }
    }
  }
}
