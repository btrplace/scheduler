package btrplace.solver.api.cstrSpec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Fabien Hermenier
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CstrTestsProvider {
    String name();

    String constraint();

    String source() default "";
}


/**

 @CTest(replay="spread.json")
 public void testFoo(Verifier v) {
 v.timeout(5).maxFailures(1).maxTests(10000).workers(7);
 v.verify();
 }

 public ReconfigurationPlanFuzzer myFuzzer() {
 ReconfigurationPlanFuzzer f = new ReconfigurationPlanFuzzer();
 return f;
 }

 @CTest(fuzzer="myFuzzer")
 public void testBar(Verifier v) {

 }

 Phase 1, generate testcases for a given constraint. Need to integrate language extension

 Phase 2, run the testcases in a particular settings (type of verification)
 need to integration runtime extension (typically views, adapters & co
 */