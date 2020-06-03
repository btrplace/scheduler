/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.prop;

import org.btrplace.safeplace.testing.verification.spec.Context;

/**
 * A logical proposition.
 *
 * @author Fabien Hermenier
 */
public interface Proposition {

    Proposition False = new Proposition() {
        @Override
        public Proposition not() {
            return True;
        }

        @Override
        public Boolean eval(Context m) {
            return Boolean.FALSE;
        }

        @Override
        public String toString() {
            return "false";
        }
    };

    Proposition True = new Proposition() {
        @Override
        public Proposition not() {
            return False;
        }

        @Override
        public Boolean eval(Context m) {
            return Boolean.TRUE;
        }

        @Override
        public String toString() {
            return "true";
        }
    };

    Proposition not();

    Boolean eval(Context m);
}
