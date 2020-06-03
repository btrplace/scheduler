/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.ScriptBuilder;
import org.btrplace.btrpsl.ScriptBuilderException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.constraint.Lonely;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link LonelyBuilder}.
 *
 * @author Fabien Hermenier
 */
@Test
public class LonelyBuilderTest {

    @DataProvider(name = "badLonelys")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{"lonely({VM1,VM2},{VM3});"},
                new String[]{"lonely({});"},
                new String[]{"lonely(@N[1..10]);"},
                new String[]{"lonely(VMa);"},
                new String[]{"lonely();"},
        };
    }

    @Test(dataProvider = "badLonelys", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n" + str);
        } catch (ScriptBuilderException ex) {
            System.out.println(str + " " + ex.getMessage());
            throw ex;
        }
    }

    @DataProvider(name = "goodLonelys")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{">>lonely({VM1});", 1, false},
                new Object[]{"lonely(VM1);", 1, true},
                new Object[]{">>lonely(VM[1..5]);", 5, false},
        };
    }

    @Test(dataProvider = "goodLonelys")
    public void testGoodSignatures(String str, int nbVMs, boolean c) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        Lonely x = (Lonely) b.build("namespace test; VM[1..10] : tiny;\n" + str).getConstraints().iterator().next();
        Assert.assertEquals(x.getInvolvedVMs().size(), nbVMs);
        Assert.assertEquals(x.isContinuous(), c);
    }
}
