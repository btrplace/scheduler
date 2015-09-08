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

package org.btrplace.safeplace.verification.spec;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class StringEnumVerifDomain implements VerifDomain<String> {

    private Set<String> dom;

    private int lb, ub;

    public StringEnumVerifDomain(String[] strs) {
        dom = new HashSet<>(strs.length);
        Collections.addAll(dom, strs);
    }

    @Override
    public Set<String> domain() {
        return dom;
    }

    @Override
    public String type() {
        return "string";
    }

    @Override
    public String toString() {
        return dom.toString();
    }

    public VerifDomain<String> clone() {
        return new StringEnumVerifDomain(dom.toArray(new String[dom.size()]));
    }
}
