package btrplace.solver.api.cstrSpec.spec;

import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Primitive;
import btrplace.solver.api.cstrSpec.spec.term.Var;
import btrplace.solver.api.cstrSpec.spec.term.func.*;
import btrplace.solver.api.cstrSpec.spec.type.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class SymbolsTable {

    private Map<String, Var> table;

    private Map<String, Constraint> cstrs;

    private Map<String, Function> funcs;

    private SymbolsTable parent;

    public SymbolsTable() {
        this(null);
    }

    public SymbolsTable(SymbolsTable p) {
        table = new HashMap<>();
        this.cstrs = new HashMap<>();
        this.funcs = new HashMap<>();
        parent = p;
    }

    public static SymbolsTable newBundle() {
        SymbolsTable syms = new SymbolsTable();
        newBundle(syms);
        return syms;
    }

    public static void newBundle(SymbolsTable syms) {
        syms.put(new Primitive("vm", VMType.getInstance()));
        syms.put(new Primitive("node", NodeType.getInstance()));
        syms.put(new Primitive("vmState", VMStateType.getInstance(), VMStateType.getInstance().domain(null)));
        syms.put(new Primitive("nodeState", NodeStateType.getInstance(), NodeStateType.getInstance().domain(null)));
        syms.put(new Primitive("int", IntType.getInstance()));
        syms.put(new Primitive("bool", BoolType.getInstance()));
        syms.put(new Primitive("real", RealType.getInstance()));
        syms.put(new Primitive("string", StringType.getInstance()));
        syms.put(new Host());
        syms.put(new Hosted());
        syms.put(new Running());
        syms.put(new Ready());
        syms.put(new Sleeping());
        syms.put(new Cons());
        syms.put(new Capa());
        syms.put(new Colocated());
        syms.put(new VMState());
        syms.put(new NodeState());
        syms.put(new Card());
        syms.put(new Sum());
        syms.put(new P());
    }


    public SymbolsTable enterScope() {
        return new SymbolsTable(this);
    }

    public SymbolsTable leaveScope() {
        return parent;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Variables:\n").append(table.toString()).append('\n');
        b.append("Functions:\n").append(funcs.toString()).append('\n');
        b.append("Constraints:\n").append(cstrs.toString()).append('\n');
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
        if (funcs.containsKey(id)) {
            return funcs.get(id);
        }
        if (parent != null) {
            return parent.getFunction(id);
        }
        return null;
    }


    public boolean put(Var v) {
        if (table.containsKey(v.label())) {
            return false;
        }
        table.put(v.label(), v);
        return true;
    }

    public Var getVar(String n) {
        if (table.containsKey(n)) {
            return table.get(n);
        }
        if (parent != null) {
            return parent.getVar(n);
        }
        return null;
    }

    public void put(Constraint cstr) {
        cstrs.put(cstr.id(), cstr);
    }

    public Constraint getConstraint(String id) {
        if (cstrs.containsKey(id)) {
            return cstrs.get(id);
        }
        return parent == null ? null : parent.getConstraint(id);
    }

}
