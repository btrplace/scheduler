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
public class Not implements Proposition {

  private final Proposition p;

    public Not(Proposition p) {
        this.p = p;
    }

    @Override
    public Proposition not() {
        return p;
    }

    @Override
    public Boolean eval(Context m) {
        return !p.eval(m);
    }

    @Override
    public String toString() {
        return "~" + p;
    }
}
