package btrplace.solver.api.cstrSpec.verification;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.ReconfigurationPlanChecker;
import btrplace.plan.ReconfigurationPlanCheckerException;
import btrplace.plan.TimedBasedActionComparator;
import btrplace.plan.event.Action;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.prop.Proposition;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.specChecker.PlanChecker;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class SpecVerifier implements Verifier2 {

    private static Comparator<Action> startFirstComparator = new TimedBasedActionComparator();

    private CheckerResult checkModel(Proposition p, Proposition notP, Model mo) {
        Boolean bOk = p.eval(mo);
        Boolean bKO = notP.eval(mo);

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

            PlanChecker chk = new PlanChecker();
            ReconfigurationPlanChecker rchk = new ReconfigurationPlanChecker();
            rchk.addChecker(chk);

            if (discrete) {
                Model res = p.getResult();
                if (res == null) {
                    //Core constraint violation
                    return new CheckerResult(false, new ReconfigurationPlanCheckerException(null, res, true));

                }
                return checkModel(good, noGood, res);
            } else {
                try {
                    rchk.check(p);
                } catch (Exception e) {
                    return new CheckerResult(false, e);
                }
            }
        } finally {
            cstr.reset();
        }
        return CheckerResult.newSucess();
    }

    private void setInputs(Constraint c, Model src, List<Constant> values) {
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
            if (!var.set(src, values.get(i).eval(src))) {
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
