package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.type.SetType;
import btrplace.solver.api.cstrSpec.type.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class SymbolsTable {

    private Map<String, Variable> table;

    public SymbolsTable() {
        table = new HashMap<>();

        //Predefined variables

    }

    public Variable getVariable(String n) {
        return table.get(n);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, Variable> e : table.entrySet()) {
            Variable v = e.getValue();
            b.append(v.label()).append(": ").append(v.type());
            b.append("\n");
        }
        return b.toString();
    }

    public Variable newVariable(String lbl, String op, Type t) {
        System.err.println("new variable '" + lbl + "' " + op + " " + t);
        if (table.containsKey(lbl)) {
            return null;
        }
        Type newType;
        switch (op) {
            case ":": newType = ((SetType)t).subType(); break;
            case "<:": newType = new SetType(t); break;
            default:
                throw new RuntimeException("Unsupported type in declaration: " + op);
        }
        Variable v = new Variable(lbl, newType);
        //System.err.println("\tinferred type: " + newType);
        table.put(v.label(), v);
        return v;
    }
    public Variable get(String n) {
        return table.get(n);
    }
}
