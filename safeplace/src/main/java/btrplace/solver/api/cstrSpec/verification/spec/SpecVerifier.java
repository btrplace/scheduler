package btrplace.solver.api.cstrSpec.verification.spec;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.ReconfigurationPlanCheckerException;
import btrplace.plan.TimedBasedActionComparator;
import btrplace.plan.event.Action;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.prop.Proposition;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.CheckerResult;
import btrplace.solver.api.cstrSpec.verification.Verifier2;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class SpecVerifier implements Verifier2 {

    private static Comparator<Action> startFirstComparator = new TimedBasedActionComparator();

    private CheckerResult checkModel(Proposition p, Proposition notP, Model mo) {
        SpecModel m = new SpecModel(mo);
        Boolean bOk = p.eval(m);
        Boolean bKO = notP.eval(m);

        if (bOk == null || bKO == null) {
            return CheckerResult.newError(new RuntimeException("Both null !\ngood:" + p + "\nnotGood: " + notP + "\n" + mo.getMapping().toString()));
        }
        if (bOk && bKO) {
            return CheckerResult.newError(new RuntimeException("Good _and_ bad !\ngood:" + p + "\nnotGood: " + notP + "\n" + mo.getMapping().toString()));
        } else if (!(bOk || bKO)) {
            return CheckerResult.newError(new RuntimeException("Nor good or bad !\ngood:" + p + "\nnotGood: " + notP + "\n" + mo.getMapping().toString()));
        }
        return new CheckerResult(bOk, null);
    }

    @Override
    public CheckerResult verify(Constraint cstr, ReconfigurationPlan p, List<Constant> values, boolean discrete) {
        try {
            Model src = p.getOrigin();

            setInputs(cstr, src, values);

            Proposition good = cstr.getProposition();
            Proposition noGood = good.not();

            //ReconfigurationSimulator chk = new ReconfigurationSimulator();
            SpecReconfigurationPlanChecker spc = new SpecReconfigurationPlanChecker();

            if (discrete) {
                Model res = p.getResult();
                if (res == null) {
                    //Core constraint violation
                    return new CheckerResult(false, new ReconfigurationPlanCheckerException(null, res, true));

                }
                return checkModel(good, noGood, res);
            } else {
                try {
                    spc.check(p, good, noGood);
                } catch (Exception e) {
//                    e.printStackTrace();
                    return new CheckerResult(false, e);
                }
            }
        } finally {
            cstr.reset();
        }
        return CheckerResult.newSucess();
    }

    private void setInputs(Constraint c, Model src, List<Constant> values) {
        SpecModel m = new SpecModel(src);
        //Check signature
        if (values.size() != c.getParameters().size()) {
            throw new IllegalArgumentException(toString(c.id(), values) + " cannot match " + signatureToString(c));
        }
        //System.out.println(values);
        for (int i = 0; i < values.size(); i++) {
            UserVar var = c.getParameters().get(i);
            Type t = values.get(i).type();
            if (!var.type().equals(t)) {
                throw new IllegalArgumentException(toString(c.id(), values) + " cannot match " + signatureToString(c));
            }
        }

        for (int i = 0; i < values.size(); i++) {
            UserVar var = c.getParameters().get(i);
            if (!var.set(m, values.get(i).eval(m))) {
                throw new IllegalArgumentException("Unable to set '" + var.label() + "' (type '" + var.type() + "') to '" + values.get(i) + "'");
            }
        }
    }

    public static String signatureToString(Constraint c) {
        StringBuilder b = new StringBuilder(c.id());
        b.append('(');
        Iterator<UserVar> ite = c.getParameters().iterator();
        while (ite.hasNext()) {
            b.append(ite.next().type());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }

    public static String toString(String id, List<Constant> args) {
        StringBuilder b = new StringBuilder(id);
        b.append('(');
        Iterator<Constant> ite = args.iterator();
        while (ite.hasNext()) {
            b.append(ite.next().type());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }
}
