/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.prop;

import org.btrplace.safeplace.testing.verification.spec.Context;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class PropositionTest {

    @Test
    public void testTrue() {
        Proposition t = Proposition.True;
        Assert.assertEquals(t.eval(new Context()), Boolean.TRUE);
        Assert.assertEquals(t.toString(), "true");
    }

    @Test
    public void testFalse() {
        Proposition t = Proposition.False;
        Assert.assertEquals(t.toString(), "false");
        Assert.assertEquals(t.eval(new Context()), Boolean.FALSE);
    }
}
