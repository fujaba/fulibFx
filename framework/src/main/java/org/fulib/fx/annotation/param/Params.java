package org.fulib.fx.annotation.param;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * Methods annotated with this annotation will be called with the values of the specified parameters when using the {@link org.fulib.fx.FulibFxApp#show(String, Map)} method.
 * <p>
 * Order is important, the order of the parameters in the annotation has to match the order of the parameters in the method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Params {

    /**
     * The names of the parameter which should be injected.
     *
     * @return The name of the parameter.
     */
    String[] value();

}
