/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.prop;

import org.btrplace.safeplace.testing.verification.spec.Context;

/**
 * Logical and between several propositions.
 *
 * @author Fabien Hermenier
 */
public class And extends BinaryProp {

    public And(Proposition p1, Proposition p2) {
        super(p1, p2);
    }

    @Override
    public String operator() {
        return "&";
    }

    @Override
    public Or not() {
        return new Or(p1.not(), p2.not());
    }

    @Override
    public Boolean eval(Context m) {

        Boolean r1 = p1.eval(m);
        Boolean r2 = p2.eval(m);
        if (r1 == null || r2 == null) {
            return null;
        }
        if (!r1) {
            return false;
        }
        return r2;
    }

    @Override
    public String toString() {
        if (p1 == Proposition.True) {
            return p2.toString();
        }
        if (p2 == Proposition.True) {
            return p1.toString();
        }
        return super.toString();
    }

}