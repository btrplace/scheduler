/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.type;

/**
 * @author Fabien Hermenier
 */
public class ActionType implements Atomic {

  private static final ActionType instance = new ActionType();

    private ActionType() {
    }

    public static ActionType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "action";
    }
}
