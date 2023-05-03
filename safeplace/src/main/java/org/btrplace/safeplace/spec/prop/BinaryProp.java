/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.prop;

/**
 * A sequence of propositions having a same operator.
 *
 * @author Fabien Hermenier
 */
public abstract class BinaryProp implements Proposition {

    protected Proposition p1;
    protected Proposition p2;

    protected BinaryProp(Proposition p1, Proposition p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public String toString() {
        return p1.toString() + " " + operator() + " " + p2.toString();
    }

    public abstract String operator();

    public Proposition first() {
        return p1;
    }

    public Proposition second() {
        return p2;
    }
}
