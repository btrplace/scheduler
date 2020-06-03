/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.ScriptBuilder;
import org.btrplace.btrpsl.ScriptBuilderException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Node;
import org.btrplace.model.constraint.SatConstraint;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link OfflineBuilder}.
 *
 * @author Fabien Hermenier
 */
@Test
public class OfflineBuilderTest {

    @DataProvider(name = "badOfflines")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{"offline({});"},
                new String[]{">>offline({VM7});"},
                new String[]{"offline({@N[1..5]});"},
        };
    }

    @Test(dataProvider = "badOfflines", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;" + str);
        } catch (ScriptBuilderException ex) {
            System.out.println(str + " " + ex.getMessage());
            throw ex;
        }
    }

    @DataProvider(name = "goodOfflines")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{">>offline(@N1);", 1, false},
                new Object[]{">>offline(@N[1..10]);", 10, false}
        };
    }

    @Test(dataProvider = "goodOfflines")
    public void testGoodSignatures(String str, int nbNodes, boolean c) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        Set<SatConstraint> cstrs = b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;" + str).getConstraints();
        Assert.assertEquals(cstrs.size(), nbNodes);
        Set<Node> nodes = new HashSet<>();
        for (SatConstraint x : cstrs) {
            Assert.assertTrue(nodes.addAll(x.getInvolvedNodes()));
            Assert.assertEquals(x.getInvolvedNodes().size(), 1);
            Assert.assertEquals(x.isContinuous(), c);
        }
    }
}
