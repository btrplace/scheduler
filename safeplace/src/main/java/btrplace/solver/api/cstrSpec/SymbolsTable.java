package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.type.*;

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
        table.put(VMType.getInstance().label(), new Variable(VMType.getInstance().label(), VMType.getInstance()));
        table.put(NodeType.getInstance().label(), new Variable(NodeType.getInstance().label(), NodeType.getInstance()));
        table.put(NatType.getInstance().label(), new Variable(NatType.getInstance().label(), NatType.getInstance()));
        /*register(NatType.getInstance());
        register(VMStateType.getInstance());
        register(NodeStateType.getInstance());*/

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
        //System.err.println("new variable '" + lbl + "' " + op + " " + t);
        if (table.containsKey(lbl)) {
            return null;
        }
        Type newType;
        switch (op) {
            case ":":
                if (t instanceof  SetType) {
                    newType = ((SetType)t).subType(); break;
                } else {
                    //It will be a primitive type
                    newType = t;
                }

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
