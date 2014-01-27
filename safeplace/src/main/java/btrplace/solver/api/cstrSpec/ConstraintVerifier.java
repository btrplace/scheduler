package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.TimedBasedActionComparator;
import btrplace.plan.event.Action;
import btrplace.solver.api.cstrSpec.spec.prop.Proposition;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import btrplace.solver.api.cstrSpec.spec.type.Type;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class ConstraintVerifier {

    private static Comparator<Action> startFirstComparator = new TimedBasedActionComparator();

    public Boolean eval(Constraint cstr, ReconfigurationPlan p, List<Constant> values) {

        Model src = p.getOrigin();

        setInputs(cstr, src, values);

        Proposition good = cstr.getProposition();
        Proposition noGood = good.not();

        Model res = src.clone();
        List<Action> actions = new ArrayList<>(p.getActions());
        Collections.sort(actions, startFirstComparator);
        //System.out.println(p);
        for (Action a : actions) {
            Boolean bOk = cstr.getProposition().eval(src);
            Boolean bKO = cstr.getProposition().not().eval(src);

            if (bOk == null || bKO == null) {
                throw new RuntimeException("Both null !\ngood:" + good + "\nnotGood: " + noGood + "\n" + res.getMapping().toString());
            }
            if (bOk && bKO) {
                throw new RuntimeException(values + " good and bad !\ngood:" + good + "\nnotGood: " + noGood + "\n" + res.getMapping().toString());
            } else if (!(bOk || bKO)) {
                throw new RuntimeException("Nor good or bad !\ngood:" + good + "\nnotGood: " + noGood + "\n" + res.getMapping().toString());
            }
            //System.out.println("Good: " + bOk + " noGood: " + bKO);
            if (!bOk) {
                return false;
            }
            //System.out.println("Apply " + a + " on \n" + res.getMapping());
            if (!a.apply(res)) {
                System.out.println("Unable to apply " + a + " on \n" + res.getMapping());
                return false; //Not applyable
            }
            //System.out.println(a + " applied successfully");
        }
        cstr.reset();
        return true;
    }

    private void setInputs(Constraint c, Model src, List<Constant> values) {
        //Check signature
        if (values.size() != c.getParameters().size()) {
            throw new IllegalArgumentException(toString(c.id(), values) + " cannot match " + signatureToString(c));
        }
        System.out.println(values);
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
