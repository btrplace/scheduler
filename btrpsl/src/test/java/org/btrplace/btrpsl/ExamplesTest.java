/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl;

import org.btrplace.btrpsl.includes.PathBasedIncludes;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
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
        ((PathBasedIncludes) scrBuilder.getIncludes()).addPath(new File("src/test/resources/org/btrplace/btrpsl/examples"));

        //Parse myApp.btrp
        Script myApp = scrBuilder.build(new File("src/test/resources/org/btrplace/btrpsl/examples/myApp.btrp"));
        Assert.assertEquals(myApp.getVMs().size(), 24);
        Assert.assertEquals(myApp.getNodes().size(), 0);
        Assert.assertEquals(myApp.getConstraints().size(), 5);

        //Check the resulting mapping
        Mapping map = mo.getMapping();
        Assert.assertEquals(map.getReadyVMs().size(), 24);
        Assert.assertEquals(map.getOfflineNodes().size(), 251);

    }
}

