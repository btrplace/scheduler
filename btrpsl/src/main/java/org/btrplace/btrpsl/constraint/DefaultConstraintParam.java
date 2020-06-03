/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

/**
 * @author Fabien Hermenier
 */
public abstract class DefaultConstraintParam<E> implements ConstraintParam<E> {

  private final String name;

  private final String paramType;

    /**
     * Make a new number parameter.
     *
     * @param n the parameter value
     * @param t the parameter type
     */
    public DefaultConstraintParam(String n, String t) {
        this.name = n;
        this.paramType = t;
    }

    @Override
    public String prettySignature() {
        return paramType;
    }

    @Override
    public String fullSignature() {
        return name + ": " + paramType;
    }


    @Override
    public String getName() {
        return name;
    }
}
