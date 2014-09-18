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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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

/* TODO: To remove (debug purpose only)
    @Test
    public void fromEntropyConverterTest() throws IOException {

        String[] params = new String[6];
        params[0] = "/user/vkherbac/home/Documents/btrplace/wkld-tdsc/r3/nr/0-src.pbd";
        params[1] = "/user/vkherbac/home/Documents/btrplace/wkld-tdsc/r3/nr/0-dst.pbd";
        params[2] = "/user/vkherbac/home/Documents/btrplace/wkld-tdsc/r3/c33p5000/datacenter.btrp";
        params[3] = "/user/vkherbac/home/Documents/btrplace/wkld-tdsc/r3/c33p5000/clients/c9.btrp";
        params[4] = "-o";
        params[5] = "/user/vkherbac/home/Documents/btrplace/wkld-tdsc/r3/nr/0-c33p5000-c9.json";

        String src, dst = null, output, scriptDC = null, scriptCL = null;

        if (params.length < 3 || params.length > 6 || params.length == 5 || !params[params.length-2].equals("-o")) {
            return;
        }
        src = params[0];
        output = params[params.length - 1];
        if (params.length > 3) {
            dst = params[1];
            if (params.length > 5) {
                scriptDC = params[2];
                scriptCL = params[3];
            }
        }

        OutputStreamWriter out = null;
        try {
            // Convert the src file
            ConfigurationConverter conv = new ConfigurationConverter(src);
            Instance i = conv.getInstance();

            // Read the dst file, deduce and add the states constraints
            if (dst != null) {
                i.getSatConstraints().addAll(conv.getNextStates(dst));
            }

            // Read the script files
            ScriptBuilder scriptBuilder = new ScriptBuilder(i.getModel());
            scriptBuilder.setIncludes(new PathBasedIncludes(scriptBuilder,
                    new File("/user/vkherbac/home/Documents/btrplace/wkld-tdsc/r3/c33p5000")));
            // Read the datacenter script file if exists
            if (scriptDC != null) {
                String strScriptDC = null;
                try {
                    strScriptDC = readFile(scriptDC);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Script scrDC = null;
                try {
                    // Build the DC script
                    scrDC = scriptBuilder.build(strScriptDC);

                } catch (ScriptBuilderException sbe) {
                    System.out.println(sbe);
                }
                // Set the DC script as an include
                //BasicIncludes bi = new BasicIncludes();
                //bi.add(scrDC);
                //scriptBuilder.setIncludes(bi);
            }
            // Read the client script file if exists
            if (scriptCL != null) {
                String strScriptCL = null;
                try {
                    strScriptCL = readFile(scriptCL);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Script scrCL = null;
                try {
                    // Build the DC script
                    scrCL = scriptBuilder.build(strScriptCL);

                } catch (ScriptBuilderException sbe) {
                    System.out.println(sbe);
                }

                // Add the resulting constraints
                if (scrCL.getConstraints() != null) {
                    i.getSatConstraints().addAll(scrCL.getConstraints());
                }

            }

            // Convert to JSON
            InstanceConverter iConv = new InstanceConverter();
            JSONObject o = iConv.toJSON(i);

            if (output.endsWith(".gz")) {
                out = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(output)));
            } else {
                out = new FileWriter(output);
            }

            // Write the output file
            o.writeJSONString(out);
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(1);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
            }
        }
    }
*/


    /**
     * read a file
     *
     * @param fileName
     * @return the file content as a String
     * @throws IOException
     */
    private String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }
}

