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
public class Or extends BinaryProp {

    public Or(Proposition p1, Proposition p2) {
        super(p1, p2);
    }

    @Override
    public String operator() {
        return "|";
    }

    @Override
    public And not() {
        return new And(p1.not(), p2.not());
    }

    @Override
    public Boolean eval(Context m) {
        Boolean r1 = p1.eval(m);
        if (r1 == null) {
            return null;
        }
        if (r1) {
            return true;
        }
        return p2.eval(m);
    }

    @Override
    public String toString() {
        if (p1 == Proposition.False) {
            return p2.toString();
        }
        if (p2 == Proposition.False) {
            return p1.toString();
        }
        return super.toString();
    }
}
