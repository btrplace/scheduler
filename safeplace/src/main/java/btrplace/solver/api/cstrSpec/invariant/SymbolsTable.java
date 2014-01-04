package btrplace.solver.api.cstrSpec.invariant;

import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.invariant.func.*;
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

    private Map<String, Constraint> cstrs;

    private List<Primitive> primitives;

    private Map<String, Function2> funcs;

    public SymbolsTable() {
        table = new HashMap<>();
        this.primitives = new ArrayList<>();
        this.cstrs = new HashMap<>();
        this.funcs = new HashMap<>();
    }

    public static SymbolsTable newBundle() {
        SymbolsTable syms = new SymbolsTable();
        syms.declare(new Primitive("vm", VMType.getInstance()));
        syms.declare(new Primitive("node", NodeType.getInstance()));
        syms.declare(new Primitive("vmState", VMStateType.getInstance(), VMStateType.getInstance().domain(null)));
        syms.declare(new Primitive("nodeState", NodeStateType.getInstance(), NodeStateType.getInstance().domain(null)));
        syms.declare(new Primitive("nat", IntType.getInstance()));
        syms.declare(new Primitive("string", StringType.getInstance()));
        syms.put(new Host());
        syms.put(new Hosted());
        syms.put(new Cons());
        syms.put(new Capa());
        syms.put(new Colocated());
        syms.put(new VMState());
        syms.put(new NodeState());
        syms.put(new Card());
        syms.put(new Sum());
        return syms;
    }

    public static void newBundle(SymbolsTable syms) {
        syms.declare(new Primitive("vm", VMType.getInstance()));
        syms.declare(new Primitive("node", NodeType.getInstance()));
        syms.declare(new Primitive("vmState", VMStateType.getInstance(), VMStateType.getInstance().domain(null)));
        syms.declare(new Primitive("nodeState", NodeStateType.getInstance(), NodeStateType.getInstance().domain(null)));
        syms.declare(new Primitive("nat", IntType.getInstance()));
        syms.declare(new Primitive("string", StringType.getInstance()));
        syms.put(new Host());
        syms.put(new Hosted());
        syms.put(new Cons());
        syms.put(new Capa());
        syms.put(new Colocated());
        syms.put(new VMState());
        syms.put(new NodeState());
        syms.put(new Card());
        syms.put(new Sum());
    }


    public void resetLocal() {
        this.table.clear();
        this.primitives.clear();
        newBundle(this);
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

    public boolean put(Function2 f) {
        if (funcs.containsKey(f.id())) {
            return false;
        }
        funcs.put(f.id(), f);
        return true;
    }

    public Function2 getFunction(String id) {
        return funcs.get(id);
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

    public void declare(Constraint cstr) {
        cstrs.put(cstr.id(), cstr);
    }

    public Constraint getConstraint(String id) {
        return cstrs.get(id);
    }

}
