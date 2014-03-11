package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.fuzzer.ModelsGenerator;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzerListener;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.TestCaseConverter;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.btrplace.CheckerVerifier;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Verify {

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

    private static int nbVMs = 1;
    private static int nbNodes = 1;
    private static int minDuration = 1;
    private static int maxDuration = 3;
    private static int nbDurations = 10;
    private static int nbDelays = 10;
    private static String specFile = null;
    private static String cstrId = null;
    private static String verifier = "impl";
    private static String json = null;
    private static boolean q = false;
    private static boolean continuous = true;

    private static ReconfigurationPlanFuzzer fuzzer;

    private static void exit(String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    private static Constraint makeConstraint(String specFile, String cstrId, boolean cont) {
        if (specFile == null) {
            exit("Missing specFile");
        }
        if (cstrId == null) {
            exit("Missing constraint id");
        }

        SpecReader r = new SpecReader();
        Specification spec = null;
        try {
            spec = r.getSpecification(new File(specFile));
        } catch (Exception e) {
            exit("Unable to parse '" + specFile + "': " + e.getMessage());
        }
        Constraint c = spec.get(cstrId);
        if (c == null) {
            exit("No specification for constraint '" + cstrId + "'");
        }
        return c;
    }

    private static List<Verifier> makeVerifier(String verifier) {
        List<Verifier> verifiers = new ArrayList<>();
        verifiers.add(new SpecVerifier());
        switch (verifier) {
            case "impl":
                verifiers.add(new ImplVerifier(false));
                break;
            case "impl_repair":
                verifiers.add(new ImplVerifier(true));
                break;
            case "checker":
                verifiers.add(new CheckerVerifier());
                break;
            default:
                exit("Unsupported verifier: '" + verifier);
        }
        return verifiers;
    }

    private static void serialize(List<TestCase> issues, String output) {
        TestCaseConverter tcc = new TestCaseConverter();
        if (output == null) {
            return;
        }
        try (FileWriter implF = new FileWriter(output)) {
            tcc.toJSON(issues, implF);
        } catch (Exception e) {
            exit(e.getMessage());
        }
    }

    private static void summarize() {
        System.out.println("Size of the model: " + nbVMs + " VM(s), " + nbNodes + " node(s)");
        System.out.println("Verifier: " + verifier);
        System.out.println("Action duration: " + minDuration + ".." + maxDuration);
        System.out.println("nbDurations per action per plan: " + (nbDurations == -1 ? "all" : nbDurations));
        System.out.println("nbDelay per action per plan: " + (nbDelays == -1 ? "all" : nbDelays));
        System.out.println("restriction mode: " + (continuous ? "continuous" : "discrete"));
        if (json != null) {
            System.out.println("Output file: " + json);
        }
        System.out.println("Nb of models to test: " + fuzzer.nbSourceModel());
        System.out.println("Max nb. of basic plan per model: " + fuzzer.maxNbOfPlansPerModel());
        System.out.println("Max nb. of plan with diff. durations: " + fuzzer.maxNbOfDurationsPerPlan());
    }

    private static void usage() {
        System.out.println("Verify [options] specFile cstr_id");
        System.out.println("\tVerify the constraint 'cstr_id' using its specification available in 'specFile'");
        System.out.println("\nOptions:");
        System.out.println("--verifier (impl | impl_repair | checker)\tthe verifier to compare to. Default is 'impl'");
        System.out.println("--restriction (continuous | discrete)\tThe type of restriction  to consider for the constraint (if supported). Default is continuous");
        System.out.println("--size VxN\tmake a model of V vms and N nodes. Default is 1x1");
        System.out.println("--durations min..sup\taction duration vary from min to sup (incl). Default is 1..3");
        System.out.println("--nbDurations (all|nb)\tnb of different durations per scheduling. 'all' for all possible. Default is 10");
        System.out.println("--nbDelays (all|nb)\tnb of different scheduling delay per plan. 'all' for all possible. Default is 10");
        System.out.println("--json out\tthe JSON file where failures are stored. Default is no output");
        System.out.println("--quiet no output.");
        System.out.println("-h || --help\tprint this help");
        System.exit(1);
    }

    public static void main(String[] args) {
        /*
         --spec foo.cspec
         --constraint c
         --size 2x3
         --durations 1..3
         (--verifier impl || impl_repair || checker)
         (--mode continuous || discrete)
         --nbDurations (all | nb)
         --nbDelays (all || nb)
         --quiet
         (--json file)
         */

        //Parse arguments
        final Counter counter = new Counter();
        int i;
        boolean endOptions = false;
        for (i = 0; i < args.length; i++) {
            String k = args[i];
            switch (k) {
                case "--verifier":
                    verifier = args[++i];
                    break;
                case "--restriction":
                    continuous = args[++i].equals("continuous");
                    break;
                case "--nbDelays":
                    nbDelays = args[++i].equals("all") ? -1 : Integer.parseInt(args[i]);
                    break;
                case "--nbDurations":
                    nbDurations = args[++i].equals("all") ? -1 : Integer.parseInt(args[i]);
                    break;
                case "--durations":
                    String[] toks = args[++i].split("\\.\\.");
                    minDuration = Integer.parseInt(toks[0]);
                    maxDuration = Integer.parseInt(toks[1]);
                    break;
                case "--size":
                    String[] ts = args[++i].split("x");
                    nbVMs = Integer.parseInt(ts[0]);
                    nbNodes = Integer.parseInt(ts[1]);
                    break;
                case "--json":
                    json = args[++i];
                    break;
                case "-h":
                case "--help":
                    usage();
                    break;
                case "--quiet":
                    q = true;
                default:
                    endOptions = true;
                    break;
            }
            if (endOptions) {
                break;
            }
        }
        if (args.length - i < 2) {
            System.err.println("Missing arguments");
            usage();

        }
        specFile = args[i++];
        cstrId = args[i++];
        final Constraint c = makeConstraint(specFile, cstrId, continuous);
        final List<Verifier> verifiers = makeVerifier(verifier);

        final List<TestCase> issues = new ArrayList<>();

        if (continuous) {
            fuzzer = new ReconfigurationPlanFuzzer(nbVMs, nbNodes).minDuration(minDuration).maxDuration(maxDuration)
                    .nbDelays(nbDelays).nbDurations(nbDurations);

            if (!q) {
                summarize();
            }
            fuzzer.addListener(new ReconfigurationPlanFuzzerListener() {
                @Override
            public void recv(ReconfigurationPlan p) {
                    verify2(verifiers, c, p, !continuous, issues, counter);
                }
            });
            fuzzer.go();
        } else {
            ModelsGenerator mg = new ModelsGenerator(nbNodes, nbVMs);
            for (Model m : mg) {
                ReconfigurationPlan p = new DefaultReconfigurationPlan(m);
                verify2(verifiers, c, p, !continuous, issues, counter);
            }
        }
        if (!q) {
            System.out.println();
            System.out.println(issues.size() + "/" + counter.get() + " failure(s)");
            for (TestCase tc : issues) {
                System.out.println(tc.pretty(true));
            }
        }

        serialize(issues, json);
    }

    private static void verify2(List<Verifier> verifiers, Constraint c, ReconfigurationPlan p, boolean discrete, List<TestCase> issues, Counter counter) {
        if (c.isCore()) {
            TestCase tc3 = new TestCase(verifiers, c, p, Collections.<Constant>emptyList(), !continuous);
            verify(tc3, issues, counter);
        } else {
            SpecModel mo = new SpecModel(p.getOrigin());
            ConstraintInputGenerator tig = new ConstraintInputGenerator(c, mo, true);
            for (List<Constant> params : tig) {
                TestCase tc3 = new TestCase(verifiers, c, p, params, !continuous);
                verify(tc3, issues, counter);
            }
        }
    }

    private static void verify(TestCase tc, List<TestCase> issues, Counter c) {
        c.hit();
        if (!tc.succeed()) {
            if (!q) {
                System.out.print("-");
            }
            issues.add(tc);
        } else {
            if (!q) {
                System.out.print("+");
            }
        }
        if (!q && c.get() % 80 == 0) {
            System.out.println();
        }
    }
}
