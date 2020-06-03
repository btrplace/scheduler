/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.prop;

import org.btrplace.safeplace.testing.verification.spec.Context;

/**
 * @author Fabien Hermenier
 */
public class Iff extends BinaryProp {

  private final Or o;


    public Iff(Proposition p1, Proposition p2) {
        super(p1, p2);
        o = new Or(new And(p1, p2), new And(p1.not(), p2.not()));
    }

    @Override
    public String operator() {
        return "<-->";
    }

    @Override
    public And not() {
        return o.not();
    }

    @Override
    public Boolean eval(Context m) {
        return o.eval(m);
    }
}
