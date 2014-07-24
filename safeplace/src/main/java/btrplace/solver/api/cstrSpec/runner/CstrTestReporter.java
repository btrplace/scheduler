package btrplace.solver.api.cstrSpec.runner;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import eu.infomas.annotation.AnnotationDetector;

import java.lang.annotation.Annotation;

/**
 * @author Fabien Hermenier
 */
public class CstrTestReporter implements AnnotationDetector.MethodReporter {

    @Override
    public void reportMethodAnnotation(Class<? extends Annotation> annotation, String className, String methodName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends Annotation>[] annotations() {
        return new Class[]{CstrTest.class};
    }
}
