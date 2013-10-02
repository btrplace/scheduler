package btrplace.solver.api.cstrSpec;

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
    }

    public boolean def(String n, Type t) {
        if (table.containsKey(n)) {
            return false;
        }
        Variable v = new Variable(n , t);
        table.put(n, v);

        t.newValue(n);
        return true;
    }

    public boolean isDeclared(String n) {
        return table.containsKey(n);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, Variable> e : table.entrySet()) {
            b.append(e.getKey()).append(" : ").append(e.getValue());
            b.append("\n");
        }
        return b.toString();
    }

    public Variable get(String n) {
        return table.get(n);
    }
}
