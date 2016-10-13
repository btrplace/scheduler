/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.safeplace.testing;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.InstanceConverter;
import org.btrplace.json.plan.ReconfigurationPlanConverter;
import org.btrplace.model.Instance;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.view.ModelView;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.spec.term.Constant;
import org.btrplace.safeplace.testing.verification.btrplace.ScheduleConverter;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class TestCase {

    private Instance instance;

    private ReconfigurationPlan plan;

    private Constraint cstr;

    private List<Constant> args;

    private SatConstraint impl;

    private List<String> groups;
    public TestCase(Instance i, ReconfigurationPlan plan, Constraint cstr) {
        instance = i;
        this.plan = plan;
        this.cstr = cstr;
        this.args = Collections.emptyList();
        groups = new ArrayList<>();
    }

    public Constraint constraint() {
        return cstr;
    }

    public List<Constant> args() {
        return args;
    }

    public TestCase args(List<Constant> args) {
        this.args = Collections.unmodifiableList(args);
        return this;
    }

    public ReconfigurationPlan plan() {
        return plan;
    }

    public Instance instance() {
        return instance;
    }

    public TestCase impl(SatConstraint s) {
        impl = s;
        return this;
    }

    public List<String> groups() {
        return groups;
    }

    public SatConstraint impl() {
        return this.impl;
    }

    public TestCase with(String arg, Object v) {
        return this;
    }

    public boolean continuous() {
        return impl() == null || impl().isContinuous();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCase testCase = (TestCase) o;
        /*System.err.println("----");
        if (!Objects.equals(instance, testCase.instance)) {
            System.err.println(instance.getSatConstraints() + "\n" + testCase.instance.getSatConstraints());
        }
        System.err.println(Objects.equals(plan, testCase.plan));
        System.err.println(Objects.equals(cstr, testCase.cstr));
        System.err.println(Objects.equals(args, testCase.args));
        if (!Objects.equals(impl, testCase.impl)) {
            System.err.println(impl + "\n" + testCase.impl());
        }*/

        //System.err.println(Objects.equals(groups, testCase.groups));

        return Objects.equals(instance, testCase.instance) &&
                Objects.equals(plan, testCase.plan) &&
                Objects.equals(cstr, testCase.cstr) &&
                Objects.equals(args, testCase.args) &&
                Objects.equals(impl, testCase.impl) &&
                Objects.equals(groups, testCase.groups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instance, plan, cstr, args, impl, groups);
    }

    @Override
    public String toString() {
        String restriction = "continuous ";
        if (impl() != null && !impl().isContinuous()) {
            restriction = "discrete ";
        }
        return "Constraint: " + restriction + cstr.toString(args) + "\n"
                + instance.getModel().getMapping() + "\n"
                + instance.getModel().getViews().stream().map(ModelView::toString).collect(Collectors.joining("\n","","\n"))
                + plan + "\n";
    }

    public String toJSON() throws JSONConverterException {
        InstanceConverter ic = new InstanceConverter();
        ic.getConstraintsConverter().register(new ScheduleConverter());
        ReconfigurationPlanConverter pc = new ReconfigurationPlanConverter();
        JSONObject o = new JSONObject();
        o.put("constraint", constraint().id());
        JSONArray a = new JSONArray();
        for (Constant c : args()) {
            a.add(c.toJSON());
        }
        o.put("args", a);
        o.put("continuous", continuous());
        o.put("groups", groups());
        o.put("plan", pc.toJSON(plan()));
        o.put("instance", ic.toJSON(instance()));
        return o.toJSONString();
    }

    public static TestCase fromJSON(List<Constraint> cstrs , String c) throws ParseException, JSONConverterException {

        JSONParser p = new JSONParser(JSONParser.MODE_RFC4627);
        JSONObject o = (JSONObject) p.parse(new StringReader(c));
        String cId = o.getAsString("constraint");
        Constraint cstr = cstrs.stream().filter(x -> x.id().equals(cId)).findFirst().get();
        InstanceConverter ic = new InstanceConverter();
        ic.getConstraintsConverter().register(new ScheduleConverter());
        ReconfigurationPlanConverter rc = new ReconfigurationPlanConverter();
        TestCase tc = new TestCase(ic.fromJSON(o.getAsString("instance")), rc.fromJSON(o.getAsString("plan")), cstr);
        List<Constant> l = new ArrayList<>();
        for(Object x : (JSONArray) o.get("args")) {
            l.add(Constant.fromJSON((JSONObject) x));
        }
        tc.args(l);
        if (cstr.isSatConstraint()) {
            tc.impl(cstr.instantiate(l.stream().map(x -> x.eval(null)).collect(Collectors.toList())));
        }
        if (tc.impl() != null) {
            tc.impl().setContinuous((Boolean)o.get("continuous"));
        }
        return tc;
    }
}
