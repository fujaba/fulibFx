package org.fulib.fx.annotation.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods annotated with this annotation will be upon the initialization of the controller/component.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface onInit {

    /**
     * The order in which the method should be called. Lower values are called first.
     *
     * @return the order in which the method should be called
     */
    int value() default 0;

    int LOWEST = Integer.MIN_VALUE;
    int LOW = -10;
    int DEFAULT = 0;
    int HIGH = 10;
    int HIGHEST = Integer.MAX_VALUE;
}
