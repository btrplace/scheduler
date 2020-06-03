/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.ScriptBuilder;
import org.btrplace.btrpsl.ScriptBuilderException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.constraint.ResourceCapacity;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ResourceCapacityBuilder}.
 *
 * @author Fabien Hermenier
 */
@Test
public class ResourceCapacityBuilderTest {

    @DataProvider(name = "badResources")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{">>resourceCapacity({@N1,@N2},\"foo\", -1);"},
                new String[]{"resourceCapacity({},\"foo\", 5);"},
                new String[]{">>resourceCapacity(@N[1,3,5]);"},
                new String[]{"resourceCapacity(\"foo\");"},
                new String[]{"resourceCapacity(VM[1..3],\"foo\", 3);"},
                new String[]{">>resourceCapacity(@N[1..3],\"foo\", 3.2);"},
                new String[]{"resourceCapacity(5);"},
                new String[]{"resourceCapacity(\"bar\", \"foo\", 5);"},
        };
    }

    @Test(dataProvider = "badResources", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;\n" + str);
        } catch (ScriptBuilderException ex) {
            System.out.println(str + " " + ex.getMessage());
            throw ex;
        }
    }

    @DataProvider(name = "goodResources")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{">>resourceCapacity(@N1,\"foo\", 3);", 1, "foo", 3, false},
                new Object[]{"resourceCapacity(@N[1..4],\"foo\", 7);", 4, "foo", 7, true},
                new Object[]{">>resourceCapacity(@N[1..3],\"bar\", 7-5%2);", 3, "bar", 6, false},
        };
    }

    @Test(dataProvider = "goodResources")
    public void testGoodSignatures(String str, int nbNodes, String rcId, int capa, boolean c) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        ResourceCapacity x = (ResourceCapacity) b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;\n" + str).getConstraints().iterator().next();
        Assert.assertEquals(x.getInvolvedNodes().size(), nbNodes);
        Assert.assertEquals(x.getResource(), rcId);
        Assert.assertEquals(x.getAmount(), capa);
        Assert.assertEquals(x.isContinuous(), c);
    }
}
