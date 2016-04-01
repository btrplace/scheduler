package org.btrplace.scheduler.choco.debug;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.InstanceConverter;
import org.btrplace.model.Instance;
import org.btrplace.model.VMState;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.view.network.Network;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.DefaultParameters;
import org.btrplace.scheduler.choco.duration.LinearToAResourceActionDuration;
import org.btrplace.scheduler.choco.runner.SolutionStatistics;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;
import org.btrplace.scheduler.choco.transition.MigrateVMTransition;
import org.chocosolver.solver.exception.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * @author Vincent Kherbache
 */
public class RandomVMsScale {

    String path = new File("").getAbsolutePath() +
            "/choco/src/test/java/org/btrplace/scheduler/choco/debug/instances/";

    @Test
    public void test() throws Exception {
        SolvingStatistics ss = schedule_mvm("x1/instance_slow_38.json");
        //SolvingStatistics ss = schedule_btrplace("x1/instance_slow_38.json");
        System.out.println("Duration : " + duration(ss));
        System.out.flush();
    }

    public double duration(SolvingStatistics s) throws Exception {
        SolutionStatistics x = s.getSolutions().get(0);
        return x.getTime() + s.getCoreRPBuildDuration() + s.getSpeRPDuration();
    }

    public SolvingStatistics schedule_mvm(String instanceName) throws SchedulerException,ContradictionException {

        ReconfigurationPlan p;
        Instance i = loadInstanceFromJSON(instanceName);

        if (i == null) return null;

        // Set parameters
        DefaultParameters ps = new DefaultParameters();
        ps.setVerbosity(0);
        ps.setTimeLimit(60);
        ps.setMaxEnd(7200);
        ps.doOptimize(false);

        // Set the custom migration transition
        ps.getTransitionFactory().remove(ps.getTransitionFactory().getBuilder(VMState.RUNNING, VMState.RUNNING));
        ps.getTransitionFactory().add(new MigrateVMTransition.Builder());

        // Set a custom objective
        DefaultChocoScheduler sc = new DefaultChocoScheduler(ps);

        try {
            p = sc.solve(i);
            Assert.assertNotNull(p);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return sc.getStatistics();
    }

    public SolvingStatistics schedule_btrplace(String instanceName) throws SchedulerException,ContradictionException {

        ReconfigurationPlan p;
        Instance ii = loadInstanceFromJSON(instanceName);

        if (ii == null) return null;

        Instance i = new Instance(ii.getModel(), ii.getSatConstraints(), new MinMTTR());

        // Set parameters
        DefaultParameters ps = new DefaultParameters();
        ps.setVerbosity(0);
        ps.setTimeLimit(60);
        //ps.setMaxEnd(600);
        ps.doOptimize(false);

        // Set custom duration evaluator
        ps.getDurationEvaluators().register(MigrateVM.class, new LinearToAResourceActionDuration<>("mem", 8));

        // Detach the network view
        i.getModel().detach(i.getModel().getView(Network.VIEW_ID));

        // Set a custom objective
        DefaultChocoScheduler sc = new DefaultChocoScheduler(ps);

        try {
            p = sc.solve(i);
            Assert.assertNotNull(p);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return sc.getStatistics();
    }

    public Instance loadInstanceFromJSON(String fileName) {

        // Read the input JSON file
        JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        Object obj = null;
        try {
            // Check for gzip extension
            if (fileName.endsWith(".gz")) {
                obj = parser.parse(new InputStreamReader(new GZIPInputStream(new FileInputStream(path + fileName))));
            } else {
                obj = parser.parse(new FileReader(path + fileName));
            }
        } catch (ParseException e) {
            System.err.println("Error during XML file parsing: " + e.toString());
            return null;
        } catch (FileNotFoundException e) {
            System.err.println("File '"+fileName+"' not found (" + e.toString() + ")");
            return null;
        } catch (IOException e) {
            System.err.println("IO error while loading plan: " + e.toString());
            return null;
        }
        JSONObject o = (JSONObject) obj;

        InstanceConverter instanceConverter = new InstanceConverter();
        try {
            return instanceConverter.fromJSON(o);
        } catch (JSONConverterException e) {
            System.err.println("Error while converting plan: " + e.toString());
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}

