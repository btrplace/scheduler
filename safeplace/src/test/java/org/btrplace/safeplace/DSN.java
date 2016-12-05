/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.safeplace;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.spec.SpecScanner;
import org.btrplace.safeplace.testing.Bench;
import org.btrplace.safeplace.testing.Result;
import org.btrplace.safeplace.testing.TestCampaign;
import org.btrplace.safeplace.testing.TestScanner;
import org.btrplace.safeplace.testing.fuzzer.Restriction;
import org.btrplace.safeplace.testing.reporting.CSVReporting;
import org.btrplace.safeplace.testing.reporting.StoredReporting;
import org.btrplace.safeplace.testing.verification.Verifier;
import org.btrplace.safeplace.testing.verification.btrplace.CheckerVerifier;
import org.btrplace.safeplace.testing.verification.spec.SpecVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class DSN {

    public static String root = "xp-dsn";

    public TestScanner newScanner() throws Exception {
        SpecScanner specScanner = new SpecScanner();
        List<Constraint> l = specScanner.scan();
        Bench.mode = Bench.Mode.REPLAY;
        return new TestScanner(l);
    }

    @Test
    public void fuzzingSizing() throws Exception {
        TestScanner sc = newScanner();
        Path path = Paths.get(root,"fuzz.csv");
        Files.deleteIfExists(path);

        for (int p = 100; p <= 1000; p+=100) {
            for (int s = 2; s <= 20; s+=2) {
                System.out.println("--- Population: " + p + " scale: " + s + " ---");
                Bench.reporting = new CSVReporting(path, Integer.toString(p));
                Bench.population = p;
                Bench.scale = s;
                List<TestCampaign> campaigns = sc.testGroups("sides");
                if (campaigns.isEmpty()) {
                    Assert.fail("Nothing to test");
                }
                System.out.println(campaigns.stream().mapToInt(TestCampaign::go).sum());
            }
        }
    }

    @Test
    public void fuzzingScalability() throws Exception {
        TestScanner sc = newScanner();

        //Warm-up
        Bench.transitions = true;

        Path p = Paths.get(root, "testing-speed-notrans.csv");
        Files.deleteIfExists(p);

     /*   for (int i = 10; i <= 30; i+=2) {
            Bench.transitions = false;
            Bench.population = 100;
            Bench.scale = i;
            System.out.println("--- scaling factor " + i + "; transitions= " + Bench.transitions +" ---");
            Bench.reporting = new CSVReporting(p,"");
            System.out.println(sc.test(Bench.class).stream().mapToInt(TestCampaign::go).sum());
        }*/


        //GOGO
        p = Paths.get(root, "testing-speed-notrans.csv");
        Files.deleteIfExists(p);
        Bench.mode = Bench.Mode.DEFAULT;
        for (int i = 1; i <= 30; i += 2) {
            Bench.transitions = false;
            Bench.population = 100;
            Bench.scale = i;
            System.out.println("--- scaling factor " + i + "; transitions= " + Bench.transitions + " ---");
            Bench.reporting = new CSVReporting(p, "");
            System.out.println(sc.test(Bench.class).stream().mapToInt(TestCampaign::go).sum());
        }

        p = Paths.get(root, "testing-speed-trans.csv");
        Files.deleteIfExists(p);

        for (int i = 1; i <= 30; i+=2) {
            System.out.println("--- scaling factor " + i + "; transitions= " + Bench.transitions +" ---");
            Bench.transitions = true;
            Bench.population = 100;
            Bench.scale = i;
            Bench.reporting = new CSVReporting(p,"");
            System.out.println(sc.test(Bench.class).stream().mapToInt(TestCampaign::go).sum());
        }
    }

    @Test
    public void specLength() throws Exception {
        SpecScanner sc = new SpecScanner();
        List<Constraint> l = sc.scan();
        System.out.println(l.stream().map(Constraint::pretty).collect(Collectors.joining("\n")));

        Path path = Paths.get(root, "inv.csv");
        String out = l.stream()
                .map(c -> Integer.toString(c.proposition().toString().length()))
                .collect(Collectors.joining("\n"));
        Files.write(path, out.getBytes());

        List<Integer> funcs = new ArrayList<>();
        List<Path> paths = Files.list(Paths.get("safeplace/src/main/java/org/btrplace/safeplace/spec/term/func"))
                .filter(Files::isRegularFile).collect(Collectors.toList());
        for (Path p : paths) {
            try (InputStream in = Files.newInputStream(p)) {
                CompilationUnit cu = JavaParser.parse(in);
                new FunctionVisitor(funcs).visit(cu, null);
            }
        }
        path = Paths.get(root, "func.csv");
        out = funcs.stream()
                        .map(c -> Integer.toString(c))
                        .collect(Collectors.joining("\n"));
        Files.write(path, out.getBytes());
    }

    @Test
    public void funcFrequency() throws Exception {
        SpecScanner sc = new SpecScanner();
        List<Constraint> l = sc.scan();
        Pattern p = Pattern.compile("([a-zA-Z]+\\()+");
        Map<String, Integer> map = new HashMap<>();
        for (Constraint c : l) {
            String prop = c.proposition().toString();
            Matcher m = p.matcher(prop);
            System.out.println(prop);
            int start = 0;
            while (m.find(start)) {
                String name = prop.substring(m.start(), m.end() - 1);
                if (Character.isLowerCase(name.charAt(0))) {
                    if (!map.containsKey(name)) {
                        map.put(name, 1);
                    } else {
                        map.put(name, map.get(name) + 1);
                    }
                }
                System.out.println("\t" + prop.substring(m.start(), m.end() - 1));
                start = m.end();
            }
        }
        System.out.println(map);
        Path out = Paths.get(root, "func-freq.csv");
        Files.deleteIfExists(out);
        String cnt = "name;freq\n" +
                    map.entrySet().stream().map(e -> e.getKey()+";" + e.getValue() + "\n").collect(Collectors.joining());
        Files.write(out, cnt.getBytes());

    }

    @Test
    public void specVsCheckers() throws Exception {
        TestScanner sc = newScanner();
        Bench.population = 500;
        Bench.scale = 10;
        Path p = Paths.get(root, "verifier_stable.csv");
        Files.deleteIfExists(p);
        boolean first = true;
        for (Verifier v : new Verifier[]{new SpecVerifier(), new CheckerVerifier()}) {
            if (first) {
                Bench.mode = Bench.Mode.SAVE;
                first = !first;
            } else {
                Bench.mode = Bench.Mode.REPLAY;
            }
            System.out.println("--- Verifier: " + v.getClass() + " ---");
            Bench.reporting = new CSVReporting(p, v.id());
            System.out.println(sc.test(Bench.class).stream().mapToInt(t -> {t.verifyWith(v); return t.go();}).sum());
        }
    }

    @Test
    public void discreteVsContinuous() throws Exception {
        TestScanner sc = newScanner();
        Bench.population = 500;
        Bench.scale = 10;
        Path path = Paths.get(root, "restriction_stable.csv");
        Files.deleteIfExists(path);
        boolean first = true;
        for (Restriction r : EnumSet.allOf(Restriction.class)) {
            if (first) {
                Bench.mode = Bench.Mode.SAVE;
                first = !first;
            } else {
                Bench.mode = Bench.Mode.REPLAY;
            }
            System.out.println("--- Restriction: " + r + "; replay= " + first + " ---");
            Bench.reporting = new CSVReporting(path, r.toString());
            System.out.println(sc.testGroups("bi").stream().mapToInt(x -> {
                x.fuzz().restriction(EnumSet.of(r));
                return x.go();
            }).sum());
        }
    }

    @Test
    public void repairVsRebuild() throws Exception {
        TestScanner sc = newScanner();
        Bench.population = 500;
        Bench.scale = 10;
        Path path = Paths.get(root, "mode_stable.csv");
        Files.deleteIfExists(path);
        boolean first = true;
        for (boolean repair : new boolean[]{false, true}) {
            if (first) {
                Bench.mode = Bench.Mode.SAVE;
                first = !first;
            } else {
                Bench.mode = Bench.Mode.REPLAY;
            }
            System.out.println("--- Repair: " + repair + "; replay= " + first + " ---");
                Bench.reporting = new CSVReporting(path, repair ? "enabled" : "disabled");
            System.out.println(sc.test(Bench.class).stream().mapToInt(x -> {
                x.schedulerParams().doRepair(true);
                return x.go();
            }).sum());
        }
    }

    @Test
    public void errors() throws Exception {
        TestScanner sc = newScanner();
        Bench.source = "xp-dsn";
        Bench.mode = Bench.Mode.REPLAY;
        Bench.population = 1000;
        Bench.scale = 5;
        Path p = Paths.get(root, "errors.csv");
        Files.deleteIfExists(p);
        Bench.reporting = new CSVReporting(p, "");
        System.out.println(sc.test(Bench.class).stream().mapToInt(TestCampaign::go).sum());
    }

    @Test
    public void prettyErrors() throws Exception {
        TestScanner sc = newScanner();
        Bench.source = "xp-dsn";
        Bench.mode = Bench.Mode.REPLAY;
        Bench.population = 1000;
        Bench.reporting = new StoredReporting(root).verbosity(3).capture(x -> !x.result().equals(Result.success));
        Bench.scale = 5;

        System.out.println(sc.test(Bench.class).stream().mapToInt(TestCampaign::go).sum());
    }


    @Test
    //Extract the number of line of codes of tests
    public void testSloc() throws Exception {
        //Parse the legacy unit tests
        List<Integer> unitTests = new ArrayList<>();
        List<Path> paths = Files.list(Paths.get("choco/src/test/java/org/btrplace/scheduler/choco/constraint/")).filter(Files::isRegularFile).collect(Collectors.toList());
        for (Path p : paths) {
            try (InputStream in = Files.newInputStream(p)){
                CompilationUnit cu = JavaParser.parse(in);
                new UnitTestsVisitor(unitTests).visit(cu, null);
            }
        }

        //Parse the new unit tests
        List<Integer> safeTests = new ArrayList<>();

        try (InputStream in = Files.newInputStream(Paths.get("safeplace/src/test/java/org/btrplace/safeplace/testing/TestSafePlace.java"))){
            CompilationUnit cu = JavaParser.parse(in);
            new SafeplaceTestsVisitor(safeTests).visit(cu, null);
        }

        String sb = "testing;sloc\n" +
                unitTests.stream().map(i -> "btrPlace;" + i).collect(Collectors.joining("\n", "", "\n")) +
                safeTests.stream().map(i -> "safePlace;" + i).collect(Collectors.joining("\n", "", "\n"));
        Path path = Paths.get(root, "sloc.csv");
        Files.write(path, sb.getBytes());
    }

    private static class FunctionVisitor extends VoidVisitorAdapter {

        private List<Integer> l;

        public FunctionVisitor(List<Integer> numbers) {
            this.l = numbers;
        }

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            if (n.getName().equals("eval")) {
                l.add(n.getEndLine() - n.getBeginLine());
            }
            super.visit(n, arg);
        }
    }

    private static class UnitTestsVisitor extends VoidVisitorAdapter {

        private List<Integer> l;

        public UnitTestsVisitor(List<Integer> numbers) {
            this.l = numbers;
        }

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            System.out.println(n.getName());
            if (n.toStringWithoutComments().contains("solve")) {
                l.add(n.getBody().getEndLine() - n.getBody().getBeginLine());
            }
            super.visit(n, arg);
        }
    }

    private static class SafeplaceTestsVisitor extends VoidVisitorAdapter {

        private List<Integer> l;

        public SafeplaceTestsVisitor(List<Integer> numbers) {
            this.l = numbers;
        }

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            for (AnnotationExpr a : n.getAnnotations()) {
                if (!a.getName().getName().equals("CstrTest")) {
                    return;
                }
            }
            System.out.println(n.getName());
            l.add(n.getEndLine() - n.getBeginLine());
            super.visit(n, arg);
        }
    }
}