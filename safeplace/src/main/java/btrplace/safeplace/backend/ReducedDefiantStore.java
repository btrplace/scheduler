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

package btrplace.safeplace.backend;

import btrplace.safeplace.reducer.Reducer;
import btrplace.safeplace.verification.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ReducedDefiantStore extends NoDuplicatedStore {

    private List<Reducer> reducers;

    public ReducedDefiantStore() {
        reducers = new ArrayList<>();
    }

    public ReducedDefiantStore reduceWith(Reducer r) {
        reducers.add(r);
        return this;
    }

    @Override
    public void addDefiant(TestCase c) {
        TestCase x = c;
        try {
            /*for (Reducer r : reducers) {
                x = r.reduce(x);
            } */
            super.addDefiant(x);
            //System.err.println("From " + c.pretty(true));
            //System.err.println("to " + x.pretty(true));
        } catch (Exception e) {
            super.addDefiant(c);
        }
    }
}
