/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class SpecScannerTest {

    @Test
    public void test() throws Exception {
        SpecScanner sc = new SpecScanner();
        List<Constraint> l = sc.scan();
        Assert.assertEquals(l.size(), 27);
        long from = System.currentTimeMillis();
        System.out.println(l.stream().map(Constraint::pretty).collect(Collectors.joining("\n")));
        System.out.println(System.currentTimeMillis() - from + " ms");
    }
}