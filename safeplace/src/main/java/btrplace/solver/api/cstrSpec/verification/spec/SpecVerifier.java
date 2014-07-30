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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class SpecVerifier implements Verifier {

    private List<VerifDomain> vDoms;

    public SpecVerifier(List<VerifDomain> vDoms) {
        this.vDoms = vDoms;
    }

    public SpecVerifier() {
        this(Collections.<VerifDomain>emptyList());
    }

    @Override
    public CheckerResult verify(Constraint cstr, List<Constant> values, Model dst, Model src) {
        SpecModel sRes = new SpecModel(dst);
        setInputs(cstr, sRes, values);
        Proposition ok = cstr.getProposition();
        Boolean bOk = ok.eval(sRes);

        return new CheckerResult(bOk, "");
    }

    @Override
    public CheckerResult verify(Constraint cstr, List<Constant> values, ReconfigurationPlan p) {

        Proposition good = cstr.getProposition();
        SpecModel mo = new SpecModel(p.getOrigin()); //Discrete means the plan contains no actions.
        setInputs(cstr, mo, values);
        SpecReconfigurationPlanChecker spc = new SpecReconfigurationPlanChecker(mo, p);
        try {
            Action a = spc.check(good);
            if (a != null) {
                return new CheckerResult(false, a);
            }

        } catch (Exception e) {
            return new CheckerResult(false, e.getMessage());
        }
        return CheckerResult.newSuccess();
    }

    private void setInputs(Constraint c, SpecModel mo, List<Constant> values) {
        //Check signature
        if (values.size() != c.getParameters().size()) {
            throw new IllegalArgumentException(toString(c.id(), values) + " cannot match " + signatureToString(c));
        }
        for (int i = 0; i < values.size(); i++) {
            UserVar var = c.getParameters().get(i);
            Type t = values.get(i).type();
            if (!var.type().equals(t)) {
                throw new IllegalArgumentException(toString(c.id(), values) + " cannot match " + signatureToString(c));
            }
        }

        for (int i = 0; i < values.size(); i++) {
            UserVar var = c.getParameters().get(i);
            mo.setValue(var.label(), values.get(i).eval(mo));
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
