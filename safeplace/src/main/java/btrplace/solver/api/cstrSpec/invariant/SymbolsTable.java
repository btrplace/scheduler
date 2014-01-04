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

    private Map<String, Function> funcs;

    public SymbolsTable() {
        table = new HashMap<>();
        this.primitives = new ArrayList<>();
        this.cstrs = new HashMap<>();
        this.funcs = new HashMap<>();
    }

    public static SymbolsTable newBundle() {
        SymbolsTable syms = new SymbolsTable();
        syms.put(new Primitive("vm", VMType.getInstance()));
        syms.put(new Primitive("node", NodeType.getInstance()));
        syms.put(new Primitive("vmState", VMStateType.getInstance(), VMStateType.getInstance().domain(null)));
        syms.put(new Primitive("nodeState", NodeStateType.getInstance(), NodeStateType.getInstance().domain(null)));
        syms.put(new Primitive("int", IntType.getInstance()));
        syms.put(new Primitive("real", RealType.getInstance()));
        syms.put(new Primitive("string", StringType.getInstance()));
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
        syms.put(new Primitive("vm", VMType.getInstance()));
        syms.put(new Primitive("node", NodeType.getInstance()));
        syms.put(new Primitive("vmState", VMStateType.getInstance(), VMStateType.getInstance().domain(null)));
        syms.put(new Primitive("nodeState", NodeStateType.getInstance(), NodeStateType.getInstance().domain(null)));
        syms.put(new Primitive("int", IntType.getInstance()));
        syms.put(new Primitive("real", RealType.getInstance()));
        syms.put(new Primitive("string", StringType.getInstance()));
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

    public boolean put(Function f) {
        if (funcs.containsKey(f.id())) {
            return false;
        }
        funcs.put(f.id(), f);
        return true;
    }

    public Function getFunction(String id) {
        return funcs.get(id);
    }

    public boolean put(Primitive v) {
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

    public boolean put(UserVariable v) {
        return add(v);
    }

    public Var getVar(String n) {
        return table.get(n);
    }

    public void put(Constraint cstr) {
        cstrs.put(cstr.id(), cstr);
    }

    public Constraint getConstraint(String id) {
        return cstrs.get(id);
    }

}
