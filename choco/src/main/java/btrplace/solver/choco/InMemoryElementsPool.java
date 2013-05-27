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

package btrplace.solver.choco;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;


/**
 * A in-memory implementation of {@link ElementPool}.
 * ints are generated in the ascending order.
 *
 * @author Fabien Hermenier
 */
public class InMemoryElementsPool implements ElementPool {

    private final Stack<Integer> available;

    private int next;

    private long free;

    private final Set<Integer> used = new HashSet<>();

    public static final long DEFAULT_SIZE = Long.MAX_VALUE;

    /**
     * Make a new pool with a default size of {@link #DEFAULT_SIZE}
     */
    public InMemoryElementsPool() {
        this(DEFAULT_SIZE);
    }

    /**
     * Make a new pool of element.
     */
    public InMemoryElementsPool(long s) {
        free = s;
        next = 0;
        available = new Stack<>();
    }

    @Override
    public int request() {
        synchronized (used) {
            if (free <= 0) {
                return -1;
            }
            int r;
            if (available.isEmpty()) {
                next++;
                return next;
            } else {
                free--;
                r = available.pop();
            }
            used.add(r);
            return r;
        }
    }

    @Override
    public boolean release(int u) {
        synchronized (used) {
            if (used.contains(u)) {
                free++;
                available.push(u);
                return used.remove(u);
            }
        }
        return false;
    }

    @Override
    public boolean inUse(int u) {
        return used.contains(u);
    }

    @Override
    public boolean book(int u) {
        return used.add(u);
    }
}
