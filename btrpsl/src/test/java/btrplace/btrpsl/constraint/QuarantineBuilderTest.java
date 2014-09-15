/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.btrpsl.constraint;

import btrplace.btrpsl.ScriptBuilder;
import btrplace.btrpsl.ScriptBuilderException;
import btrplace.model.DefaultModel;
import btrplace.model.Node;
import btrplace.model.constraint.Quarantine;
import btrplace.model.constraint.SatConstraint;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link QuarantineBuilder}.
 *
 * @author Fabien Hermenier
 */
@Test
public class QuarantineBuilderTest {

    @DataProvider(name = "badQuarantines")
    public Object[][] getBadSignatures() {
        return new String[][]{
                new String[]{"quarantine({});"},
                new String[]{"quarantine({VM7});"},
                new String[]{">>quarantine(@N1);"}, //quarantine is necessarily continuous
                new String[]{"quarantine({@N[1..5]});"},
        };
    }

    @Test(dataProvider = "badQuarantines", expectedExceptions = {ScriptBuilderException.class})
    public void testBadSignatures(String str) throws ScriptBuilderException {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        try {
            b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;\n" + str);
        } catch (ScriptBuilderException ex) {
            System.out.println(str + " " + ex.getMessage());
            throw ex;
        }
    }

    @DataProvider(name = "goodQuarantines")
    public Object[][] getGoodSignatures() {
        return new Object[][]{
                new Object[]{"quarantine(@N1);", 1},
                new Object[]{"quarantine(@N[1..10]);", 10}
        };
    }

    @Test(dataProvider = "goodQuarantines")
    public void testGoodSignatures(String str, int nbNodes) throws Exception {
        ScriptBuilder b = new ScriptBuilder(new DefaultModel());
        Set<SatConstraint> cstrs = b.build("namespace test; VM[1..10] : tiny;\n@N[1..20] : defaultNode;\n" + str).getConstraints();
        Assert.assertEquals(cstrs.size(), nbNodes);
        Set<Node> nodes = new HashSet<>();
        for (SatConstraint x : cstrs) {
            Assert.assertTrue(x instanceof Quarantine);
            Assert.assertTrue(nodes.addAll(x.getInvolvedNodes()));
        }

    }
}
