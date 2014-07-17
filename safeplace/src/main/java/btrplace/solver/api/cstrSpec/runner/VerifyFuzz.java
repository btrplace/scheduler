package btrplace.solver.api.cstrSpec.runner;

import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.Specification;
import btrplace.solver.api.cstrSpec.backend.InMemoryBackend;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.TransitionTable;
import btrplace.solver.api.cstrSpec.guard.ErrorGuard;
import btrplace.solver.api.cstrSpec.guard.MaxTestsGuard;
import btrplace.solver.api.cstrSpec.guard.TimeGuard;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.TestCaseConverter;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.btrplace.CheckerVerifier;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.IntVerifDomain;
import btrplace.solver.api.cstrSpec.verification.spec.StringEnumVerifDomain;
import btrplace.solver.api.cstrSpec.verification.spec.VerifDomain;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class VerifyFuzz {

    private static String json = null;
    private static int verbosityLvl = 0;
    private static int nbWorkers = Runtime.getRuntime().availableProcessors() - 1;

    private static List<VerifDomain> vDoms = new ArrayList<>();

    private static void exit(String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    private static Specification getSpec(String specFile) {
        if (specFile == null) {
            exit("Missing specFile");
        }
        SpecReader r = new SpecReader();
        Specification spec = null;
        try {
            return r.getSpecification(new File(specFile));
        } catch (Exception e) {
            exit("Unable to parse '" + specFile + "': " + e.getMessage());
        }
        return null;
    }

    private static Verifier makeVerifier(String verifier) {
        switch (verifier) {
            case "impl":
                return new ImplVerifier(false);
            case "impl_repair":
                return new ImplVerifier(true);
            case "checker":
                return new CheckerVerifier();
        }
        exit("Unsupported verifier: '" + verifier);
        return null;
    }

    private static void serialize(InMemoryBackend b, String output) {
        Collection<TestCase> defiant = b.getDefiant();
        Collection<TestCase> compliant = b.getCompliant();
        TestCaseConverter tcc = new TestCaseConverter();
        if (output == null) {
            return;
        }
        try (FileWriter implF = new FileWriter(output)) {
            JSONArray arr = new JSONArray();
            for (TestCase tc : defiant) {
                JSONObject o = new JSONObject();
                o.put("tc", tcc.toJSON(tc));
                o.put("succeeded", false);
                arr.add(o);
            }
            for (TestCase tc : compliant) {
                JSONObject o = new JSONObject();
                o.put("tc", tcc.toJSON(tc));
                o.put("succeeded", true);
                arr.add(o);
            }
            arr.writeJSONString(implF);
        } catch (Exception e) {
            exit(e.getMessage());
        }
    }

    private static void makeVerifDomain(String def) {
        String[] toks = def.split("=");

        if (toks[0].equals("int")) {
            String[] bounds = toks[1].split("\\.\\.");
            vDoms.add(new IntVerifDomain(Integer.parseInt(bounds[0]), Integer.parseInt(bounds[1])));
        } else if (toks[0].equals("string")) {
            vDoms.add(new StringEnumVerifDomain(toks[1].split(",")));
        }
    }

    private static void usage() {
        System.out.println("Verify [options] specFile cstr_id");
        System.out.println("\tVerify the constraint 'cstr_id' using its specification available in 'specFile'");
        System.out.println("\nOptions:");
        System.out.println("--verifier (impl | impl_repair | checker)\tthe verifier to compare to");
        System.out.println("--continuous perform a verification wrt. a continuous restriction (default)");
        System.out.println("--discrete perform a verification wrt. a discrete restriction");
        System.out.println("--size VxN\tmake a model of V vms and N nodes");
        System.out.println("--dom key=lb..ub. Search space for the given type");
        System.out.println("--durations min..sup\taction duration vary from min to sup (incl). Default is 1..3");
        System.out.println("--json out\tthe JSON file where failures are stored. Default is no output");
        System.out.println("-p nb\tNb. of verifications in parallel. Default is " + nbWorkers);
        System.out.println("-v Increment the verbosity level (up to three '-v').");
        System.out.println("-h | --help\tprint this help");
        System.exit(1);
    }

    public static void main(String[] args) {
        //Parse arguments
        int i;
        boolean endOptions = false;
        String specFile;
        String cstrId;

        /*
         spec cstr NxM verif options
         */
        specFile = args[0];
        cstrId = args[1];

        String[] ts = args[2].split("x");
        int nbVMs = Integer.parseInt(ts[0]);
        int nbNodes = Integer.parseInt(ts[1]);
        String verifier = args[3];


        ReconfigurationPlanFuzzer fuzz = null;
        try {
            fuzz = new ReconfigurationPlanFuzzer(new TransitionTable(new FileReader("node_transitions")),
                    new TransitionTable(new FileReader("vm_transitions")),
                    nbNodes, nbVMs);
        } catch (IOException ex) {
            exit(ex.getMessage());
        }

        final Specification spec = getSpec(specFile);
        final Constraint c = spec.get(cstrId);
        final Verifier v = makeVerifier(verifier);

        ParallelConstraintVerificationFuzz paraVerif = new ParallelConstraintVerificationFuzz(fuzz, vDoms, v, c);

        for (i = 4; i < args.length; i++) {
            String k = args[i];
            switch (k) {
                case "--continuous":
                    paraVerif.setContinuous(true);
                    break;
                case "--discrete":
                    paraVerif.setContinuous(false);
                    break;
                case "--dom":
                    makeVerifDomain(args[++i]);
                    break;
                case "--json":
                    json = args[++i];
                    break;
                case "-h":
                case "--help":
                    usage();
                    break;
                case "-p":
                    paraVerif.setNbWorkers(Integer.parseInt(args[++i]));
                    break;
                case "-v":
                    paraVerif.setVerbose(true);
                    verbosityLvl++;
                    break;
                case "-t":
                    paraVerif.limit(new TimeGuard(Integer.parseInt(args[++i])));
                    break;
                case "-f":
                    paraVerif.limit(new ErrorGuard(Integer.parseInt(args[++i])));
                    break;
                case "-m":
                    paraVerif.limit(new MaxTestsGuard(Integer.parseInt(args[++i])));
                    break;

                default:
                    System.err.println("Unsupported option: " + args[i]);
                    System.exit(1);
                    break;
            }
        }
        InMemoryBackend b = new InMemoryBackend();
        paraVerif.setBackend(b);

        List<Constraint> pre = makePreconditions(c, spec);
        for (Constraint x : pre) {
            paraVerif.precondition(x);
        }
        try {
            paraVerif.verify();
        } catch (Exception ex) {
            if (ex.getMessage().contains("don't") || ex.getMessage().contains("discrete")) {
                System.err.println(ex.getMessage());
                if (verbosityLvl > 0) {
                    System.out.println("-/-/- failure(s)");
                }
                System.exit(-1);
            }
            exit(ex.getMessage());
        }


        int nbD = b.getNbDefiant();
        int nbC = b.getNbCompliant();
        int falseOk = 0, falseKo = 0;
        for (TestCase tc : b.getDefiant()) {
            if (tc.falseNegative()) {
                falseKo++;
            } else if (tc.falsePositive()) {
                falseOk++;
            } else {
                System.err.println("---BUG:\n" + tc.pretty(false));
            }
        }
        if (nbD != (falseKo + falseOk)) {
            System.err.println("BUG: " + nbD + " defiant but ok=" + falseOk + " and ko=" + falseKo);
            System.exit(1);
        }
        if (verbosityLvl > 0) {
            System.out.println(falseOk + "/" + falseKo + "/" + (nbD + nbC));
        }

        if (verbosityLvl > 1) {
            System.out.println("---- Defiant TestCases ----");
            for (TestCase tc : b.getDefiant()) {
                System.out.println(tc.pretty(true));
            }

            if (verbosityLvl > 2) {
                System.out.println("---- Compliant TestCases ----");
                for (TestCase tc : b.getCompliant()) {
                    System.out.println(tc.pretty(true));
                }
            }
        }
        serialize(b, json);
        System.exit(nbD);
    }

    private static List<Constraint> makePreconditions(Constraint c, Specification spec) {
        List<Constraint> pre = new ArrayList<>();
        for (Constraint x : spec.getConstraints()) {
            if (x.isCore()) {
                pre.add(x);
            }
        }

        //In case c is a core one, we still want to be able to verify it
        pre.remove(c);
        return pre;
    }
}
