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

/**
 * Created by fabien.hermenier on 10/05/2017.
 */
public class Cell<T> {

  protected final T content;

  Cell<T> prev;

  Cell<T> next;

  public Cell(final Cell<T> prev, final T content, final Cell<T> next) {
    this.prev = prev;
    this.next = next;
    this.content = content;
  }

  @Override
  public String toString() {
    return content.toString();
  }

}
