/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.safeplace.verification;

import btrplace.json.JSONConverterException;
import btrplace.json.plan.ReconfigurationPlanConverter;
import btrplace.model.Element;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.ReconfigurationPlan;
import btrplace.safeplace.Constraint;
import btrplace.safeplace.Specification;
import btrplace.safeplace.spec.SpecReader;
import btrplace.safeplace.spec.term.Constant;
import btrplace.safeplace.spec.type.*;
import btrplace.safeplace.verification.btrplace.CheckerVerifier;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class TestCaseConverter {

    ReconfigurationPlanConverter pc = new ReconfigurationPlanConverter();

    public void toJSON(TestCase tc, File f) throws IOException, JSONConverterException {
        try (FileWriter fw = new FileWriter(f)) {
            toJSON(tc, fw);
        }
    }

    public JSONObject toJSON(TestCase tc) throws JSONConverterException {
        JSONObject jo = new JSONObject();
        jo.put("constraint", tc.getConstraint().pretty());
        jo.put("discrete", tc.isDiscrete());
        jo.put("args", argsToJSON(tc.getArguments()));
        jo.put("plan", pc.toJSON(tc.getPlan()));
        jo.put("succeed", tc.succeed());
        return jo;
    }

   /*public void toJSON(List<TestCase> l, Appendable o) throws IOException, JSONConverterException {
        JSONArray a = new JSONArray();
        for (TestCase c : l) {
            a.add(toJSON(c));
        }
        a.writeJSONString(o);
    }*/

    public String toJSONString(TestCase tc) throws JSONConverterException {
        return toJSON(tc).toJSONString();
    }

    public void toJSON(TestCase tc, Appendable o) throws IOException, JSONConverterException {
        toJSON(tc).writeJSONString(o);
    }

    public Object argsToJSON(List<Constant> c) {
        JSONArray arr = new JSONArray();
        for (Constant x : c) {
            arr.add(jsonValue(x.eval(null)));
        }
        return arr;
    }

    public TestCase fromJSON(File path) throws Exception {
        try (FileReader in = new FileReader(path)) {
            return fromJSON(in);
        }
    }

    public TestCase fromJSON(String buf) throws Exception {
        try (StringReader in = new StringReader(buf)) {
            return fromJSON(in);
        }

    }

    public TestCase fromJSON(Reader r) throws Exception {
        try {
            JSONParser p = new JSONParser(JSONParser.MODE_RFC4627);
            Object o = p.parse(r);
            if (!(o instanceof JSONObject)) {
                throw new JSONConverterException("Unable to parse a JSON object");
            }
            return fromJSON((JSONObject) o);
        } catch (ParseException ex) {
            throw new JSONConverterException(ex);
        }
    }

    public TestCase fromJSON(JSONObject o) throws Exception {
        String cstrString = o.get("constraint").toString();
        SpecReader r = new SpecReader();
        Specification spec = r.getSpecification(cstrString);
        Constraint cstr = spec.getConstraints().get(0);
        boolean discrete = Boolean.parseBoolean(o.get("discrete").toString());
        ReconfigurationPlan p = pc.fromJSON((JSONObject) o.get("plan"));
        List<Constant> args = argsFromJSON(p, (JSONArray) o.get("args"));
        return new TestCase(new CheckerVerifier(), cstr, p, args, discrete);
    }

    public List<Constant> argsFromJSON(ReconfigurationPlan p, JSONArray o) throws JSONConverterException {
        List<Constant> args = new ArrayList<>();
        for (Object c : o) {
            args.add(argFromJSON(p, (JSONObject) c));
        }
        return args;
    }

    private Constant argFromJSON(ReconfigurationPlan p, JSONObject o) throws JSONConverterException {
        String type = o.get("type").toString();
        switch (type) {
            case "int":
                return IntType.getInstance().newValue(o.get("value").toString());
            case "string":
                return StringType.getInstance().newValue(o.get("value").toString());
            case "bool":
                return BoolType.getInstance().newValue(o.get("value").toString());
            case "float":
                return RealType.getInstance().newValue(o.get("value").toString());
            case "vm":
                VM v = p.getOrigin().newVM(Integer.parseInt(o.get("value").toString()));
                return new Constant(v, VMType.getInstance());
            case "node":
                Node n = p.getOrigin().newNode(Integer.parseInt(o.get("value").toString()));
                return new Constant(n, NodeType.getInstance());
            case "set":
                return new Constant(argFromJSON(p, (JSONObject) o.get("value")), new SetType(null));
            case "list":
                return new Constant(argFromJSON(p, (JSONObject) o.get("value")), new ListType(null));
            default:
                throw new JSONConverterException("Unsupported type '" + type + "'");
        }
    }

    private Object jsonValue(Object o) {
        if (o instanceof Integer) {
            return atom("int", o);
        } else if (o instanceof Double) {
            return atom("float", o);
        } else if (o instanceof String) {
            return atom("string,", o);
        } else if (o instanceof Boolean) {
            return atom("bool", o);
        } else if (o instanceof Collection) {
            JSONObject jo = new JSONObject();
            if (o instanceof Set) {
                jo.put("type", "set");
            } else {
                jo.put("type", "list");
            }
            JSONArray a = new JSONArray();
            for (Object cnt : (Collection) o) {
                a.add(jsonValue(cnt));
            }
            jo.put("value", a);
            return jo;
        } else if (o instanceof Element) {
            return atom(o instanceof VM ? "vm" : "node", ((Element) o).id());
        } else {
            throw new UnsupportedOperationException("Unsupported type '" + o.getClass().getSimpleName() + "'");
        }
    }

    private JSONObject atom(String t, Object o) {
        JSONObject jo = new JSONObject();
        jo.put("type", t);
        jo.put("value", o);
        return jo;
    }
}
