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

package btrplace.btrpsl;

import btrplace.btrpsl.includes.PathBasedIncludes;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Unit tests that check the examples are working.
 *
 * @author Fabien Hermenier
 */
public class ExamplesTest {

    @Test
    public void testExample() throws ScriptBuilderException {

        //Set the environment
        Model mo = new DefaultModel();


        //Make the builder and add the sources location to the include path
        ScriptBuilder scrBuilder = new ScriptBuilder(mo);
        ((PathBasedIncludes) scrBuilder.getIncludes()).addPath(new File("src/test/resources/btrplace/btrpsl/examples"));

        //Parse myApp.btrp
        Script myApp = scrBuilder.build(new File("src/test/resources/btrplace/btrpsl/examples/myApp.btrp"));
        Assert.assertEquals(myApp.getVMs().size(), 24);
        Assert.assertEquals(myApp.getNodes().size(), 0);
        Assert.assertEquals(myApp.getConstraints().size(), 5);

        //Check the resulting mapping
        Mapping map = mo.getMapping();
        Assert.assertEquals(map.getReadyVMs().size(), 24);
        Assert.assertEquals(map.getOfflineNodes().size(), 251);

    }
}

