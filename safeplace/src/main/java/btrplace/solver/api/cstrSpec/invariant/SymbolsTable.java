package btrplace.solver.api.cstrSpec.invariant;

import btrplace.solver.api.cstrSpec.invariant.type.NatType;
import btrplace.solver.api.cstrSpec.invariant.type.NodeType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;
import btrplace.solver.api.cstrSpec.invariant.type.VMType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class SymbolsTable {

    private Map<String, Variable> table;

    public SymbolsTable() {
        table = new HashMap<>();

        //Predefined symbols
        table.put(VMType.getInstance().label(), new Variable(VMType.getInstance().label(), VMType.getInstance()));
        table.put(NodeType.getInstance().label(), new Variable(NodeType.getInstance().label(), NodeType.getInstance()));
        table.put(NatType.getInstance().label(), new Variable(NatType.getInstance().label(), NatType.getInstance()));
        /*register(NatType.getInstance());
        register(VMStateType.getInstance());
        register(NodeStateType.getInstance());*/

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

    public Variable declare(String lbl, Type t) {
        if (table.containsKey(lbl)) {
            return null;
        }
        Variable v = new Variable(lbl, t);
        table.put(v.label(), v);
        return v;
    }

    public Variable get(String n) {
        return table.get(n);
    }
}
