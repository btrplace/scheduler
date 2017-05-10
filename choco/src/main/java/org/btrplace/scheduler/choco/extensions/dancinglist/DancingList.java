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

import org.chocosolver.memory.IEnvironment;

/**
 * A backtrackable double-linked list using the notion of Dancing Links introduced by
 * Knuth.
 */
public class DancingList<T> {

  private Cell<T> head;

  private final IEnvironment env;

  private int size = 0;

  public DancingList(final IEnvironment env, final Iterable<T> elmts) {
    this.env = env;
    Cell<T> prev = head;
    for (final T e : elmts) {
      final Cell<T> c = new Cell<>(prev, e, null);
      if (head == null) {
        head = c;
      } else {
        prev.next = c;
      }
      size++;
      prev = c;
    }
  }

  /**
   * Get the cell after a given cell if exists.
   *
   * @param c the cell
   * @return the next cell if exists. {@code null} otherwise
   */
  public Cell<T> next(final Cell<T> c) {
    return c.next;
  }

  /**
   * Get the cell before a given cell if exists.
   *
   * @param c the cell
   * @return the previous cell if exists. {@code null} otherwise
   */
  public Cell<T> prev(final Cell<T> c) {
    return c.prev;
  }

  /**
   * Delete a cell.
   * It will be restored automatically by the environment when it will backtrack.
   *
   * @param c the cell to delete
   */
  public void delete(final Cell<T> c) {
    if (c.next != null) {
      // not at the end of the list.
      c.next.prev = c.prev;
    }
    // Corner case of the head.
    if (c.prev == null) {
      head = c.next;
    } else {
      c.prev.next = c.next;
    }
    size--;
    env.save(() -> unDelete(c));
  }

  private void unDelete(final Cell<T> c) {
    if (c.prev == null) {
      // It was the head of the list.
      head = c;
    } else {
      c.prev.next = c;
    }
    if (c.next != null) {
      // It was the tail
      c.next.prev = c;
    }
    size++;
  }

  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder("(");
    Cell<T> p = head;
    while (p != null) {
      b.append(p.content.toString());
      if (p.next != null) {
        b.append(", ");
      }
      p = p.next;
    }
    return b.append(")").toString();
  }

  /**
   * Get the head of the list.
   *
   * @return the head.
   */
  public Cell<T> head() {
    return head;
  }

  /**
   * Get the current list size.
   *
   * @return a positive integer.
   */
  public int size() {
    return size;
  }
}
