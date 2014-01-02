package btrplace.solver.api.cstrSpec.invariant;

import btrplace.solver.api.cstrSpec.invariant.type.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class SymbolsTable {

    private Map<String, Var> table;

    private List<Primitive> primitives;

    public SymbolsTable() {
        table = new HashMap<>();
        this.primitives = new ArrayList<>();
    }

    public static SymbolsTable newBundle() {
        SymbolsTable syms = new SymbolsTable();
        syms.declare(new Primitive("vm", VMType.getInstance()));
        syms.declare(new Primitive("node", NodeType.getInstance()));
        syms.declare(new Primitive("vmState", VMStateType.getInstance(), VMStateType.getInstance().domain(null)));
        syms.declare(new Primitive("nodeState", NodeStateType.getInstance(), NodeStateType.getInstance().domain(null)));
        syms.declare(new Primitive("nat", NatType.getInstance()));
        syms.declare(new Primitive("string", StringType.getInstance()));
        return syms;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, Var> e : table.entrySet()) {
            Var v = e.getValue();
            b.append(v.label()).append(": ").append(v.type());
            b.append("\n");
        }
        return b.toString();
    }

    public List<Primitive> getPrimitives() {
        return primitives;
    }

    public boolean declare(Primitive v) {
        if (add(v)) {
            primitives.add(v);
            return true;
        }
        return false;
    }

    private boolean add(Var v) {
        if (table.containsKey(v.label())) {
            return false;
        }
        table.put(v.label(), v);
        return true;
    }

    public boolean declare(UserVariable v) {
        return add(v);
    }

    public Var get(String n) {
        return table.get(n);
    }
}
