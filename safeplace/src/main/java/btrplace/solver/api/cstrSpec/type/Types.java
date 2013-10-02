package btrplace.solver.api.cstrSpec.type;

import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class Types {

    private Map<String, Type> types;

    public Types() {
        types = new HashMap<>();
        register(VM.getInstance());
        register(Node.getInstance());
        register(Nat.getInstance());
        register(VM.getInstance());
        register(VMStateType.getInstance());
        register(NodeStateType.getInstance());
    }

    public void register(Type t) {
        types.put(t.label(), t);
    }

    public Type getTypeFromLabel(String id) {
        Type t = types.get(id);
        if (t == null) {
            throw new RuntimeException("Unknown type '" + id + "'");
        }
        return t;
    }

    public Type getTypeFromValue(String n) {
        for (Map.Entry<String, Type> e : types.entrySet()) {
            Type t = e.getValue();
            if (t.isIn(n)) {
                return t;
            }
        }
        return null;
    }
}
