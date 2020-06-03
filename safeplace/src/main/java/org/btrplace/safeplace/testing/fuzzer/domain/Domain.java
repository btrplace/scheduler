/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing.fuzzer.domain;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.spec.type.Type;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public interface Domain<T> extends Term<List<T>>{
    List<T> values();

    @Override
    Type type();

    String name();

    T randomValue();

    List<T> randomSubset();

    List<List<T>> randomPacking();

}
