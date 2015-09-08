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
public class ParamsFuzzer {

    private boolean continuous = true, discrete = true;

    public ParamsFuzzer() {

    }

    public List<Constant> build(Constraint c, SpecModel mo) {
        if (c.isCore()) {
            return new ArrayList<>();
        }
        List<Constant> l = new ArrayList<>(c.getParameters().size());
        for (UserVar v : c.getParameters()) {
            l.add(new Constant(v.pick(mo), v.type()));
        }
        return l;
    }

    public boolean continuous(Constraint c) {
        //TODO: check fuzzing direction
        Random rnd = new Random();
        boolean conti = rnd.nextBoolean();
        if (!c.isDiscrete()) {
            conti = false;
        }
        return conti;
    }

    public ParamsFuzzer continuous(boolean b) {
        continuous = b;
        return this;
    }

    public ParamsFuzzer discrete(boolean b) {
        discrete = b;
        return this;
    }

    public boolean continuous() {
        return continuous;
    }

    public boolean discrete() {
        return discrete;
    }
}
