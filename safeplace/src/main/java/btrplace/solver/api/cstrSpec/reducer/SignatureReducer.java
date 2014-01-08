package btrplace.solver.api.cstrSpec.reducer;

import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.ConstraintVerifier;
import btrplace.solver.api.cstrSpec.verification.TestCase;

import java.util.ArrayList;
import java.util.Collection;
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
    public List<TestCase> reduce(TestCase c, Constraint cstr, List<Object> in) {
        //Convertit tous les sets en liste, plus maniable
        List<Object> in2 = new ArrayList<>();
        for (Object o : in) {
            if (o instanceof Collection && o instanceof Set) {
                in2.add(new ArrayList((Collection) o));
            } else {
                in2.add(o);
            }
        }
        List<TestCase> mins = new ArrayList<>();
        Boolean b = cVerif.eval(cstr, c.getPlan(), in);
        System.out.println(in + " " + b);
        reduce(c, cstr, in2, mins);

        return mins;
    }

    private boolean reduce(TestCase c, Constraint cstr, List<Object> in, List<TestCase> mins) {
        List<Object>[] parts = split(in);
        //System.out.println(Arrays.toString(parts));
        if (parts.length == 0) {
            mins.add(c);
        }
        for (List<Object> part : parts) {
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
    private List<Object>[] split(List<Object> in) {

        //System.out.println("To split:" + in);
        List<Object>[] res = new List[2];
        res[0] = new ArrayList<>(in.size());
        res[1] = new ArrayList<>(in.size());
        boolean splitted = false;
        for (int i = 0; i < in.size(); i++) {
            Object o = in.get(i);
            if (o instanceof List) {
                List col = (List) o;
                if (col.size() >= 2) {
                    splitted = true;
                    //Keep the previous items
                    append(in, 0, i, res);

                    List<Object> l1 = new ArrayList<>();
                    List<Object> l2 = new ArrayList<>();
                    for (int j = 0; j < col.size(); j++) {
                        if (j % 2 == 0) {
                            l1.add(col.get(j));
                        } else {
                            l2.add(col.get(j));
                        }
                    }
                    res[0].add(l1);
                    res[1].add(l2);
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
