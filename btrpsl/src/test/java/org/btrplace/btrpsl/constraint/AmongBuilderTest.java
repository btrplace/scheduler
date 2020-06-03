/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl.constraint;

import org.btrplace.btrpsl.ScriptBuilder;
import org.btrplace.btrpsl.ScriptBuilderException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.constraint.Among;
import org.btrplace.model.constraint.SatConstraint;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Set;

/**
 * Unit tests for {@link AmongBuilder}.
 *
 * @author Fabien Hermenier
 */
@Test
public class AmongBuilderTest {

    @DataProvider(name = "badAmongs")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{">>among(VM1,@N[1..10]);"},
                new String[]{"among(@N1,{@N[1..3],@N[4..6]});"},
                new String[]{">>among({}, {@N[1..3],@N[4..6]});"},
                new String[]{"among({VM1},{@N[1..3],@N[4..6]},{VM2});"},
                new String[]{"among({VM1},{@N[1..3],{@N[4..6]}});"},
                new String[]{"among(VM[1..5],{@N[1..10], VM[6..10]});"},
                new String[]{"among(VM[1..6],{});"},
                new String[]{"among({},{});"},
        };
    }

    @Test(dataProvider = "badAmongs", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n" + str);
        } catch (ScriptBuilderException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    @DataProvider(name = "goodAmongs")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{">>among(VM1,{{@N1},{@N2}});", 1, 1, 1, false},
                new Object[]{"among({VM1},{@N[1..5],@N[6..10]});", 1, 5, 5, true},
                new Object[]{">>among(VM[1..5],@N[1..10] / 2);", 5, 5, 5, false}
        };
    }

    @Test(dataProvider = "goodAmongs")
    public void testGoodSignatures(String str, int nbVMs, int nbNs1, int nbNs2, boolean c) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        Set<SatConstraint> cstrs = b.build("namespace test; VM[1..10] : tiny;\n@N[1..10] : defaultNode;\n" + str).getConstraints();
        Assert.assertEquals(cstrs.size(), 1);
        Among x = (Among) cstrs.iterator().next();
        Assert.assertEquals(x.getGroupsOfNodes().iterator().next().size(), nbNs1);
        Assert.assertEquals(x.getInvolvedNodes().size(), nbNs1 + nbNs2);
        Assert.assertEquals(x.getInvolvedVMs().size(), nbVMs);
        Assert.assertEquals(x.isContinuous(), c);
    }
}
