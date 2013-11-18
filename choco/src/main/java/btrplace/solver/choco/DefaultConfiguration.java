package btrplace.solver.choco;

import choco.kernel.solver.Configuration;

import java.util.Properties;

/**
 * @author Fabien Hermenier
 */
public class DefaultConfiguration extends Properties {

    public static Properties defaultProperties() {
        Properties instance = new Properties();
        instance.put(Configuration.SOLUTION_POOL_CAPACITY, 1);
        instance.put("cp.init.propagation.shaving", Boolean.FALSE);
        instance.put("cp.scheduling.horizon", 21474836);
        instance.put("cp.init.propagation.dLB", Boolean.FALSE);
        instance.put("cp.restart.after_solution", Boolean.FALSE);
        instance.put("cp.restart.nogood_recording", Boolean.FALSE);
        instance.put("cp.restart.luby", Boolean.FALSE);
        instance.put("cp.restart.geometrical", Boolean.FALSE);
        instance.put("cp.init.propagation.shaving.dLB", Boolean.FALSE);
        instance.put("cp.search.bottom_up", Boolean.FALSE);
        instance.put("cp.resolution.stop_at_first_solution", Boolean.TRUE);
        instance.put("cp.propagation.cardinality_reasonning", Boolean.TRUE);
        instance.put("cp.init.propagation.shaving.only_decision_vars", Boolean.TRUE);
        instance.put("cp.solution.restore", Boolean.TRUE);
        instance.put("cp.propagation.variables.order", 1234567);

        instance.put("cp.propagation.constraints.order", 1234567);
        instance.put("cp.propagation.cardinality_reasonning", Boolean.TRUE);
        instance.put("cp.init.propagation.shaving.only_decision_vars", Boolean.TRUE);
        instance.put("cp.solution.restore", Boolean.TRUE);
        instance.put("cp.search.limit.value",2147483647);
        instance.put("cp.resolution.policy", "SATISFACTION");
        instance.put("cp.recomputation.gap",1);
        instance.put("cp.search.limit.value",2147483647);
        instance.put("cp.restart.base",512);
        instance.put("cp.random.seed", 0);
        instance.put("cp.restart.geometrical.grow",1.2);
        instance.put("cp.restart.limit.type","UNDEF");
        instance.put("cp.real.precision", 1.0e-6);
        instance.put("cp.domain.rationHole", 0.7f);
        instance.put("cp.restart.limit.value",2147483647);
        instance.put("cp.search.limit.type", "UNDEF");
        instance.put("cp.restart.policy.limit.type","BACKTRACK");
        instance.put("cp.real.reduction", "0.99");
        instance.put("cp.restart.luby.grow", 2);
        return instance;
    }
}
