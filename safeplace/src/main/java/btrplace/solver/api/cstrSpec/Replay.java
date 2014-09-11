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

package btrplace.solver.api.cstrSpec;

import btrplace.json.JSONConverterException;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.TestCaseConverter;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Replay {

    private static int verbosityLvl = 0;

    private static int nbUnchanged = 0;
    private static int nbProgress = 0;
    private static int nbRegressions = 0;
    private static int nbSuccess = 0;
    private static List<TestCase> failures;


    /*
    * in: json with results
    * out: show regression and improvments
     */
    static class Counter {
        private int v;

        public Counter() {
            v = 0;
        }

        public int hit() {
            return ++v;
        }

        public void reset() {
            v = 0;
        }

        public int get() {
            return v;
        }
    }

    private static void exit(String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    private static void usage() {
        System.out.println("Replay [options] json_file ");
        System.out.println("\tReplay all the test cases previously verified in 'json_file'");
        System.out.println("-v Increment the verbosity level (up to three '-v').");
        System.out.println("-h || --help\tprint this help");
        System.exit(1);
    }

    public static void main(String[] args) {
        int i;
        boolean endOptions = false;
        for (i = 0; i < args.length; i++) {
            String k = args[i];
            switch (k) {
                case "-h":
                case "--help":
                    usage();
                    break;
                case "-v":
                    verbosityLvl++;
                    break;
                default:
                    endOptions = true;
                    break;
            }
            if (endOptions) {
                break;
            }
        }
        if (args.length - i < 1) {
            System.err.println("Missing arguments");
            usage();

        }
        String inputFile = args[i++];

        TestCaseConverter tcc = new TestCaseConverter();
        failures = new ArrayList<>();

        try (FileReader in = new FileReader(inputFile)) {
            JSONParser p = new JSONParser(JSONParser.MODE_RFC4627);
            Object o = p.parse(in);
            if (!(o instanceof JSONArray)) {
                throw new JSONConverterException("Unable to parse a JSON array");
            }
            JSONArray arr = (JSONArray) o;
            int nb = 0;
            for (Object vtc : arr) {
                JSONObject j = (JSONObject) vtc;
                boolean succeeded = (Boolean) j.get("succeeded");
                TestCase tc = tcc.fromJSON((JSONObject) j.get("tc"));
                redo(tc, succeeded);
                if (verbosityLvl > 1 && nb % 80 == 0) {
                    System.out.println();
                }
            }
        } catch (Exception ex) {
            exit(ex.getMessage());
        }

        if (verbosityLvl > 0) {
            System.out.println(failures.size() + "/" + (nbSuccess + failures.size()) + " failure(s): "
                    + nbProgress + " fixed;" + nbRegressions + " regression(s); " + nbUnchanged + " unchanged");
        }
        if (verbosityLvl > 2) {
            for (TestCase tc : failures) {
                System.out.println(tc.pretty(true));
            }
        }
    }

    private static void redo(TestCase tc, boolean succeeded) {
        boolean nowSucceed = tc.succeed();
        if (nowSucceed) {
            nbSuccess++;
        } else {
            failures.add(tc);
        }
        if (nowSucceed == succeeded) {
            if (verbosityLvl > 1) {
                System.out.print('.');
            }
            nbUnchanged++;
        } else if (nowSucceed) {
            if (verbosityLvl > 1) {
                System.out.print('+');
            }
            nbProgress++;
        } else {
            if (verbosityLvl > 1) {
                System.out.print('-');
            }
            nbRegressions++;
        }
    }

}
