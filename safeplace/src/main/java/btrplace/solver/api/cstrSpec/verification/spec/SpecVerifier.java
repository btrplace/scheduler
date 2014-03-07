package btrplace.solver.api.cstrSpec.verification.spec;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.prop.Proposition;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.CheckerResult;
import btrplace.solver.api.cstrSpec.verification.Verifier;

import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class SpecVerifier implements Verifier {

    @Override
    public CheckerResult verify(Constraint cstr, ReconfigurationPlan p, List<Constant> values, boolean discrete) {
        try {
            Model src = p.getOrigin();

            setInputs(cstr, src, values);

            Proposition good = cstr.getProposition();
            Proposition noGood = good.not();

            SpecReconfigurationPlanChecker spc = new SpecReconfigurationPlanChecker(p);
            if (discrete) {
                SpecModel mo = new SpecModel(p.getOrigin()); //Discrete means the plan contains no actions.
                Proposition ok = cstr.getProposition();
                Proposition ko = ok.not();
                Boolean bOk = ok.eval(mo);
                Boolean bKo = ko.eval(mo);
                if (bOk == null || bKo == null) {
                    throw new RuntimeException(ok.eval(mo) + "\n" + ko.eval(mo));
                }
                if (bOk.equals(bKo)) {
                    throw new RuntimeException("Both have the same result: " + bOk + " " + bKo);
                }
                return new CheckerResult(bOk, "");
            } else {
                Action a = spc.check(good, noGood);
                if (a != null) {
                    return new CheckerResult(false, a);
                }
            }
        } finally {
            cstr.reset();
        }
        return CheckerResult.newSuccess();
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
