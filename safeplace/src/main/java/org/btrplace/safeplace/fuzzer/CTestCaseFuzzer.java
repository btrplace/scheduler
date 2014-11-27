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

package org.btrplace.safeplace.fuzzer;

import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.safeplace.CTestCase;
import org.btrplace.safeplace.Constraint;
import org.btrplace.safeplace.spec.term.Constant;
import org.btrplace.safeplace.verification.spec.SpecModel;

import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class CTestCaseFuzzer implements Iterator<CTestCase>, Iterable<CTestCase> {

    private Constraint cstr;

    private ReconfigurationPlanFuzzer2 rpf;

    private String name;

    private ConstraintInputFuzzer argsf;

    private int nb = 1;

    private List<Constraint> pre;

    public CTestCaseFuzzer(String n, Constraint c, List<Constraint> pre, ReconfigurationPlanFuzzer2 rpf) {
        this.rpf = rpf;
        cstr = c;
        name = n;
        ReconfigurationPlan p = rpf.next();
        SpecModel mo = new SpecModel(p.getOrigin());
        /*for (VerifDomain v : rpf.doms()) {
            mo.add(v);
        } */
        argsf = new ConstraintInputFuzzer(c, mo);
        this.pre = pre;
    }

    @Override
    public Iterator<CTestCase> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public CTestCase next() {
        ReconfigurationPlan p;
        List<Constant> args;
        p = rpf.next();
        args = argsf.newParams();
        //return new CTestCase(name + "_" + (nb++), cstr, args, p);
        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }
}
