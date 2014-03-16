package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.fuzzer.ModelsGenerator;
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
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Verify {

    private static int nbVMs = 1;
    private static int nbNodes = 1;
    private static String verifier = "impl";
    private static String json = null;
    private static int verbosityLvl = 0;
    private static boolean continuous = true;
    private static int nbWorkers = Runtime.getRuntime().availableProcessors() - 1;

    private static List<VerifDomain> vDoms = new ArrayList<>();

    private static void exit(String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    private static Constraint makeConstraint(String specFile, String cstrId) {
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

    private static void serialize(List<TestCase> defiant, List<TestCase> compliant, String output) {
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
        System.out.println("--verifier (impl | impl_repair | checker)\tthe verifier to compare to. Default is '" + verifier + "'");
        System.out.println("--continuous perform a verification wrt. a continuous restriction (default)");
        System.out.println("--discrete perform a verification wrt. a discrete restriction");
        System.out.println("--size VxN\tmake a model of V vms and N nodes. Default is " + nbVMs + "x" + nbNodes);
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

        for (i = 0; i < args.length; i++) {
            String k = args[i];
            switch (k) {
                case "--verifier":
                    verifier = args[++i];
                    break;
                case "--continuous":
                    continuous = true;
                    break;
                case "--discrete":
                    continuous = false;
                    break;
                case "--dom":
                    makeVerifDomain(args[++i]);
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
                case "-p":
                    nbWorkers = Integer.parseInt(args[++i]);
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
        if (args.length - i < 2) {
            System.err.println("Missing arguments");
            usage();

        }
        specFile = args[i++];
        cstrId = args[i++];
        final Constraint c = makeConstraint(specFile, cstrId);
        final Verifier v = makeVerifier(verifier);

        ParallelConstraintVerification paraVerif =
                new ParallelConstraintVerification(new ModelsGenerator(nbNodes, nbVMs), vDoms, v, nbWorkers, c, continuous, verbosityLvl > 1);
        long startTime = System.currentTimeMillis();
        paraVerif.verify();
        long endTime = System.currentTimeMillis();

        final List<TestCase> defiant = paraVerif.getDefiant();
        final List<TestCase> compliant = paraVerif.getCompliant();

        if (verbosityLvl > 0) {
            System.out.println(defiant.size() + "/" + (defiant.size() + compliant.size()) + " failure(s); in " + (endTime - startTime) + " ms");
        }
        if (verbosityLvl > 2) {
            System.out.println("---- Defiant TestCases ----");
            for (TestCase tc : defiant) {
                System.out.println(tc.pretty(true));
            }
        }
        if (verbosityLvl > 3) {
            System.out.println("---- Compliant TestCases ----");
            for (TestCase tc : compliant) {
                System.out.println(tc.pretty(true));
            }
        }

        serialize(defiant, compliant, json);
        if (!defiant.isEmpty()) {
            System.exit(1);
        }
    }
}
