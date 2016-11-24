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
import org.btrplace.json.model.InstanceConverter;
import org.btrplace.model.Instance;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.spec.SpecScanner;
import org.btrplace.safeplace.spec.term.func.Cons;
import org.btrplace.safeplace.testing.Bench;
import org.btrplace.safeplace.testing.TestCampaign;
import org.btrplace.safeplace.testing.TestScanner;
import org.btrplace.safeplace.testing.fuzzer.Restriction;
import org.btrplace.safeplace.testing.reporting.CSVReporting;
import org.btrplace.safeplace.testing.verification.Verifier;
import org.btrplace.safeplace.testing.verification.btrplace.CSchedule;
import org.btrplace.safeplace.testing.verification.btrplace.CheckerVerifier;
import org.btrplace.safeplace.testing.verification.btrplace.Schedule;
import org.btrplace.safeplace.testing.verification.btrplace.ScheduleConverter;
import org.btrplace.safeplace.testing.verification.spec.SpecVerifier;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
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
        System.out.println("--- warm up ---");
        Path p = Paths.get(root, "foo.csv");
/*        Files.deleteIfExists(p);
        for (int i = 0; i < 2; i++) {
            Bench.scale = 3;
            Bench.reporting = new CSVReporting(p,"");
            sc.test(Bench.class).stream().mapToInt(TestCampaign::go).sum();
        }
*/
        Files.deleteIfExists(p);

        for (int i = 10; i <= 60; i+=2) {
            Bench.transitions = false;
            Bench.population = 100;
            Bench.scale = i;
            System.out.println("--- scaling factor " + i + "; transitions= " + Bench.transitions +" ---");
            Bench.reporting = new CSVReporting(p,"");
            System.out.println(sc.test(Bench.class).stream().mapToInt(TestCampaign::go).sum());
        }

        /*p = Paths.get(root, "testing-speed-trans.csv");
        Files.deleteIfExists(p);

        for (int i = 1; i <= 30; i+=2) {
            System.out.println("--- scaling factor " + i + "; transitions= " + Bench.transitions +" ---");
            Bench.transitions = true;
            Bench.scale = i;
            Bench.reporting = new CSVReporting(p,"");
            System.out.println(sc.test(Bench.class).stream().mapToInt(TestCampaign::go).sum());
        }*/
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
        Path p = Paths.get(root, "verifier.csv");
        Files.deleteIfExists(p);
        for (Verifier v : new Verifier[]{new SpecVerifier(), new CheckerVerifier()}) {
            System.out.println("--- Verifier: " + v.getClass() + " ---");
            Bench.reporting = new CSVReporting(p, v.id());
            System.out.println(sc.test(Bench.class).stream().mapToInt(t -> {t.verifyWith(v); return t.go();}).sum());
        }
    }

    @Test
    public void errors() throws Exception {
     //   Bench.mode = Bench.Mode.SAVE;
        TestScanner sc = newScanner();
        Bench.population = 1;
        Bench.scale = 3;
        Path p = Paths.get(root,"errors.csv");
        Files.deleteIfExists(p);
        Bench.reporting = new CSVReporting(p, "");
        System.out.println(sc.test(Bench.class).stream().mapToInt(TestCampaign::go).sum());
    }

    @Test
    public void generate() throws Exception {
        Bench.mode = Bench.Mode.SAVE;
        TestScanner sc = newScanner();
        Bench.population = 500;
        Bench.scale = 5;
        System.out.println(sc.test(Bench.class).stream().mapToInt(TestCampaign::go).sum());
    }

    @Test
    public void replay() throws Exception {
        Bench.mode = Bench.Mode.REPLAY;
        TestScanner sc = newScanner();
        Path p = Paths.get(root,"errors.csv");
        Files.deleteIfExists(p);
        //Bench.reporting = new CSVReporting(p, "");
        System.out.println(sc.test(Bench.class).stream().mapToInt(TestCampaign::go).sum());
    }


    @Test
    public void discreteVsContinuous() throws Exception {
        TestScanner sc = newScanner();
        Bench.population = 500;
        Bench.scale = 10;
        Path path = Paths.get(root,"restriction.csv");
        Files.deleteIfExists(path);
        for (Restriction r : EnumSet.allOf(Restriction.class)) {
            System.out.println("--- Restriction: " + r + " ---");
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
        Path path = Paths.get(root,"mode.csv");
        Files.deleteIfExists(path);
        for (boolean repair : new boolean[]{false, true}) {
                System.out.println("--- Repair: " + repair + " ---");
                Bench.reporting = new CSVReporting(path, repair ? "repair" : "rebuild");
                System.out.println(sc.test(Bench.class).stream().mapToInt(x -> {x.schedulerParams().doRepair(repair); return x.go();}).sum());
        }
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


    @Test
    public void foo() throws Exception {
        String bug = "{\"model\":{\"mapping\":{\"readyVMs\":[],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[0]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[9,3]},\"2\":{\"sleepingVMs\":[],\"runningVMs\":[]},\"3\":{\"sleepingVMs\":[],\"runningVMs\":[5]},\"4\":{\"sleepingVMs\":[],\"runningVMs\":[7,2]},\"5\":{\"sleepingVMs\":[],\"runningVMs\":[6,4]},\"6\":{\"sleepingVMs\":[],\"runningVMs\":[]},\"7\":{\"sleepingVMs\":[],\"runningVMs\":[8,1]},\"8\":{\"sleepingVMs\":[],\"runningVMs\":[]},\"9\":{\"sleepingVMs\":[],\"runningVMs\":[]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{\"0\":{\"migrate\":3},\"1\":{\"migrate\":4},\"2\":{\"migrate\":1},\"3\":{\"migrate\":1},\"4\":{\"migrate\":4},\"5\":{\"migrate\":4},\"6\":{\"migrate\":4},\"7\":{\"migrate\":1},\"8\":{\"migrate\":1},\"9\":{\"migrate\":2}}},\"views\":[{\"defConsumption\":0,\"nodes\":{\"0\":12,\"1\":14,\"2\":10,\"3\":17,\"4\":14,\"5\":13,\"6\":10,\"7\":17,\"8\":11,\"9\":15},\"rcId\":\"cpu\",\"id\":\"shareableResource\",\"defCapacity\":0,\"vms\":{\"0\":4,\"1\":3,\"2\":2,\"3\":2,\"4\":3,\"5\":5,\"6\":3,\"7\":4,\"8\":3,\"9\":2}}]},\"constraints\":[{\"vm\":7,\"start\":79,\"end\":80,\"id\":\"schedule\"},{\"nodes\":[8],\"vm\":7,\"continuous\":false,\"id\":\"fence\"},{\"vm\":7,\"id\":\"running\"},{\"rc\":\"cpu\",\"amount\":1,\"vm\":7,\"id\":\"preserve\"},{\"vm\":1,\"start\":76,\"end\":80,\"id\":\"schedule\"},{\"nodes\":[4],\"vm\":1,\"continuous\":false,\"id\":\"fence\"},{\"vm\":1,\"id\":\"running\"},{\"vm\":6,\"start\":76,\"end\":80,\"id\":\"schedule\"},{\"nodes\":[8],\"vm\":6,\"continuous\":false,\"id\":\"fence\"},{\"vm\":6,\"id\":\"running\"},{\"vm\":8,\"start\":79,\"end\":80,\"id\":\"schedule\"},{\"nodes\":[5],\"vm\":8,\"continuous\":false,\"id\":\"fence\"},{\"vm\":8,\"id\":\"running\"},{\"vm\":9,\"start\":78,\"end\":80,\"id\":\"schedule\"},{\"nodes\":[8],\"vm\":9,\"continuous\":false,\"id\":\"fence\"},{\"vm\":9,\"id\":\"running\"},{\"rc\":\"cpu\",\"amount\":4,\"vm\":9,\"id\":\"preserve\"},{\"vm\":2,\"start\":79,\"end\":80,\"id\":\"schedule\"},{\"nodes\":[9],\"vm\":2,\"continuous\":false,\"id\":\"fence\"},{\"vm\":2,\"id\":\"running\"},{\"rc\":\"cpu\",\"amount\":5,\"vm\":2,\"id\":\"preserve\"},{\"vm\":0,\"start\":77,\"end\":80,\"id\":\"schedule\"},{\"nodes\":[6],\"vm\":0,\"continuous\":false,\"id\":\"fence\"},{\"vm\":0,\"id\":\"running\"},{\"rc\":\"cpu\",\"amount\":1,\"vm\":0,\"id\":\"preserve\"},{\"vm\":3,\"start\":79,\"end\":80,\"id\":\"schedule\"},{\"nodes\":[9],\"vm\":3,\"continuous\":false,\"id\":\"fence\"},{\"vm\":3,\"id\":\"running\"},{\"vm\":5,\"start\":76,\"end\":80,\"id\":\"schedule\"},{\"nodes\":[8],\"vm\":5,\"continuous\":false,\"id\":\"fence\"},{\"vm\":5,\"id\":\"running\"},{\"vm\":4,\"start\":76,\"end\":80,\"id\":\"schedule\"},{\"nodes\":[3],\"vm\":4,\"continuous\":false,\"id\":\"fence\"},{\"vm\":4,\"id\":\"running\"},{\"node\":9,\"id\":\"online\"},{\"node\":8,\"id\":\"online\"},{\"node\":7,\"id\":\"online\"},{\"node\":6,\"id\":\"online\"},{\"node\":5,\"id\":\"online\"},{\"node\":4,\"id\":\"online\"},{\"node\":3,\"id\":\"online\"},{\"node\":2,\"id\":\"online\"},{\"node\":1,\"id\":\"online\"},{\"node\":0,\"id\":\"online\"}],\"objective\":{\"id\":\"minimizeMTTR\"}}";
        InstanceConverter ic = new InstanceConverter();
        ic.getConstraintsConverter().register(new ScheduleConverter());
        Instance i = ic.fromJSON(bug);
        DefaultChocoScheduler sched = new DefaultChocoScheduler();
        System.out.println(i.getModel());
        System.out.println(i.getSatConstraints().stream().map(c -> c.toString()).collect(Collectors.joining("\n")));
        sched.getMapper().mapConstraint(Schedule.class, CSchedule.class);
        ReconfigurationPlan p = sched.solve(i);
        sched.setVerbosity(3);
        System.err.println(sched.getStatistics());
        Assert.assertNotNull(p);
    }
}
