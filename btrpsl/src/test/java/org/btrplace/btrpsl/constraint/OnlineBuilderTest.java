/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * Unit tests for {@link OnlineBuilder}.
 *
 * @author Fabien Hermenier
 */
@Test
public class OnlineBuilderTest {

    @DataProvider(name = "badOnlines")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{"online({});"},
                new String[]{">>online({VM7});"},
                new String[]{"online({@N[1..5]});"},
        };
    }

    @Test(dataProvider = "badOnlines", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;\n" + str);
        } catch (ScriptBuilderException ex) {
            System.out.println(str + " " + ex.getMessage());
            throw ex;
        }
    }

    @DataProvider(name = "goodOnlines")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{"online(@N1);", 1, true},
                new Object[]{">>online(@N[1..10]);", 10, false}
        };
    }

    @Test(dataProvider = "goodOnlines")
    public void testGoodSignatures(String str, int nbNodes, boolean c) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        Set<SatConstraint> cstrs = b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;\n" + str).getConstraints();
        Assert.assertEquals(cstrs.size(), nbNodes);
        Set<Node> nodes = new HashSet<>();
        for (SatConstraint x : cstrs) {
            Assert.assertTrue(nodes.addAll(x.getInvolvedNodes()));
            Assert.assertEquals(x.getInvolvedNodes().size(), 1);
            Assert.assertEquals(x.isContinuous(), c);
        }
    }
}
