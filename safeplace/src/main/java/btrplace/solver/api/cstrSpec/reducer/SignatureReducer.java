package btrplace.solver.api.cstrSpec.reducer;

import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.ConstraintVerifier;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.verification.TestCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reduce a constraint signature to the possible.
 * In practice we reduce the size of the sets
 *
 * @author Fabien Hermenier
 */
public class SignatureReducer implements TestCaseReducer {
    private ConstraintVerifier cVerif = new ConstraintVerifier();

    @Override
    public List<TestCase> reduce(TestCase c, Constraint cstr, List<Constant> in) {
        //Convertit tous les sets en liste, plus maniable
        List<TestCase> mins = new ArrayList<>();
        Boolean b = cVerif.eval(cstr, c.getPlan(), in);
        System.out.println(in + " " + b);
        reduce(c, cstr, in, mins);

        return mins;
    }

    private boolean reduce(TestCase c, Constraint cstr, List<Constant> in, List<TestCase> mins) {
        List<Constant>[] parts = split(in);
        //System.out.println(Arrays.toString(parts));
        if (parts.length == 0) {
            mins.add(c);
        }
        for (List<Constant> part : parts) {
            Boolean b = cVerif.eval(cstr, c.getPlan(), in);
            System.out.println(part + " " + b);
            reduce(c, cstr, part, mins);
        }
        return true;
    }

    /**
     * Split in half the first encountered set.
     *
     * @param in
     * @return
     */
    private List<Constant>[] split(List<Constant> in) {

        //System.out.println("To split:" + in);
        List<Constant>[] res = new List[2];
        res[0] = new ArrayList<>(in.size());
        res[1] = new ArrayList<>(in.size());
        boolean splitted = false;
        for (int i = 0; i < in.size(); i++) {
            Constant o = in.get(i);
            if (o.type() instanceof SetType) {
                List col = (List) o;
                if (col.size() >= 2) {
                    splitted = true;
                    //Keep the previous items
                    append(in, 0, i, res);

                    Set<Object> l1 = new HashSet<>();
                    Set<Object> l2 = new HashSet<>();
                    for (int j = 0; j < col.size(); j++) {
                        if (j % 2 == 0) {
                            l1.add(col.get(j));
                        } else {
                            l2.add(col.get(j));
                        }
                    }
                    res[0].add(new Constant(l1, o.type()));
                    res[1].add(new Constant(l2, o.type()));
                    //res[0].add(l1);
                    //res[1].add(l2);
                    //Keep the following items
                    append(in, i + 1, in.size(), res);
                    break;
                }
            }
        }
        if (!splitted) {
            return new List[0];
        }
        return res;
    }

    private static void append(List src, int from, int to, List... dst) {
        for (int x = from; x < to; x++) {
            for (List d : dst) {
                d.add(src.get(x));
            }
        }
    }
}
