package btrplace.solver.api.cstrSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class UnitTestsExecutor {

   private List<TestResult> ok;

   private List<TestResult> ko;

    public UnitTestsExecutor() {
        ok = new ArrayList<>();
        ko = new ArrayList<>();
    }

    public void execute(TestUnit tu) {
       TestResult res = tu.verify();
       if (!res.succeeded()) {
           ko.add(res);
       } else {
           ok.add(res);
       }
    }

    public void execute(List<TestUnit> tus){
        for (TestUnit tu : tus) {
            execute(tu);
        }
    }

    public List<TestResult> getSucceeded() {
        return ok;
    }

    public List<TestResult> getFailures() {
        return ko;
    }

    @Override
    public String toString() {
        return "success: " + ok.size() + "\t failures: " + ko.size();
    }
}
