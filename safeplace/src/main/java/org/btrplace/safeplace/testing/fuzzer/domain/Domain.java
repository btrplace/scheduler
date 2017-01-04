/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.safeplace.testing.fuzzer.domain;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.spec.type.Type;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public interface Domain<T> extends Term<List<T>>{
    List<T> values();

    @Override
    Type type();

    String name();

    T randomValue();

    List<T> randomSubset();

    List<List<T>> randomPacking();

}
