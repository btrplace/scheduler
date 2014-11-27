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

import org.btrplace.safeplace.Constraint;
import org.btrplace.safeplace.spec.term.Constant;
import org.btrplace.safeplace.spec.term.UserVar;
import org.btrplace.safeplace.verification.spec.SpecModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Fabien Hermenier
 */
public class ConstraintInputFuzzer {


    private Random rnd;

    private List<Constant>[] domains;

    private SpecModel mo;

    private Constraint cstr;

    public ConstraintInputFuzzer(Constraint cstr, SpecModel mo) {
        this.mo = mo;
        this.cstr = cstr;
        domains = new ArrayList[cstr.getParameters().size()];
        rnd = new Random();

        //cache the domains
        int i = 0;
        /*for (UserVar v : cstr.getParameters()) {
            domains[i++] = v.domain(mo);
        } */
    }

    public List<Constant> newParams() {
        List<Constant> l = new ArrayList<>(domains.length);
        for (UserVar v : cstr.getParameters()) {
            l.add(new Constant(v.pick(mo), v.type()));
        }
        /*for (List<Constant> dom : domains) {
            l.add(dom.get(rnd.nextInt(dom.size())));
        } */
        //System.out.println(l);
        return l;
    }

}
