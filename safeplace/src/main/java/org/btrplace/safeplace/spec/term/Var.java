/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term;

/**
 * @author Fabien Hermenier
 */
public interface Var<T> extends Term<T> {

    String label();

    String pretty();
}
