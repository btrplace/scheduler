package org.btrplace.safeplace;


import org.btrplace.safeplace.fuzzer.Fuzzer;
import org.btrplace.safeplace.runner.Report;
import org.btrplace.safeplace.runner.TestCaseResult;
import org.btrplace.safeplace.runner.TestCasesRunner;
import org.btrplace.safeplace.scanner.Scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by fhermeni on 08/09/2015.
 */
public class FuzzTests {

    private static void usage(int c) {
        System.out.println("FuzzTests (-h) (--help) (-v)* (--tests x,y,z | --groups a,b,c) ");
        System.exit(c);
    }

    public static void main(String[] args) throws Exception {
        List<String> tests = new ArrayList<>();
        List<String> groups = new ArrayList<>();
        int verbosity = 0;
        //Fuzz --tests --groups
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--tests":
                    tests = split(args[++i]);
                    break;
                case "--groups":
                    groups = split(args[++i]);
                    break;
                case "-v":
                    verbosity++;
                    break;
                case "-h":
                case "--help":
                    usage(0);
                default:
                    usage(1);
            }
        }

        Scanner sc = new Scanner();
        tests.stream().forEach(sc::test);
        groups.stream().forEach(sc::group);

        List<TestCasesRunner> runners = sc.scan();
        System.out.println(runners.size() + " testing campaigns");
        for (TestCasesRunner r : runners) {
            Fuzzer f = sc.fuzzer(r);
            if (f == null) {
                System.err.println("No fuzzer for test case '" + r.label() + "'");
                System.exit(1);
            }
            Report report = new Report(r.label());
            List<TestCaseResult> results = r.run(f);
            for (TestCaseResult result : results) {
                if (verbosity >= 2 || (verbosity >= 1 && result.result() != TestCaseResult.Result.success)) {
                    System.out.println(result);
                }
                report.add(result);
            }
            System.out.println(report.pretty());
        }
    }

    public static List<String> split(String in) {
        return Arrays.asList(in.split(","));
    }
}
