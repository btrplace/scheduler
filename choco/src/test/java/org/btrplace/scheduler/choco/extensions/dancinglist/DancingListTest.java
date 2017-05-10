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

package org.btrplace.scheduler.choco.extensions.dancinglist;

import org.chocosolver.memory.EnvironmentBuilder;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.trailing.EnvironmentTrailing;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link DancingList}.
 *
 * @author Fabien Hermenier
 */
public class DancingListTest {

  private static List<Integer> ints(final int nb) {
    final List<Integer> l = new ArrayList<>();
    for (int i = 0; i < nb; i++) {
      l.add(i);
    }
    return l;
  }

  @Test
  public void testNew() {
    IEnvironment env = new EnvironmentTrailing();
    DancingList<Integer> l = new DancingList<>(env, ints(10));
    System.err.println(l.toString());
    Assert.assertEquals(10, l.size());

    // Iteration
    Cell<Integer> c = l.head();
    Assert.assertEquals(0, c.content.intValue());

    checkDiff(l, 1);
  }

  @Test
  public void testBacktracking() {
    IEnvironment env = new EnvironmentBuilder().fromFlat().build();
    DancingList<Integer> l = new DancingList<>(env, ints(11));
    Cell<Integer> c = l.head();
    while (c != null) {
      if (c.content % 2 == 0) {
        env.worldPush();
        l.delete(c);
      }
      c = l.next(c);
    }

    Assert.assertEquals(l.size(), 5);
    System.out.println(l);
    c = l.head();
    Assert.assertEquals(c.content.intValue(), 1);
    checkDiff(l, 2);
    for (int i = 1; i <= 6; i++) {
      env.worldPop(); // restore the 8
      Assert.assertEquals(l.size(), 5 + i);
      System.out.println(l);
    }
    Assert.assertEquals(l.size(), 11, l.toString());
    c = l.head();
    Assert.assertEquals(c.content.intValue(), 0);
    checkDiff(l, 1);
  }

  private static void checkDiff(DancingList<Integer> l, int delta) {
    Cell<Integer> p = l.head();
    while (p != null) {
      if (l.prev(p) != null) {
        Assert.assertEquals(p.content - delta, l.prev(p).content.intValue());
      }
      if (p.next != null) {
        Assert.assertEquals(p.content + delta, l.next(p).content.intValue());
      }
      p = p.next;
    }
  }
}
