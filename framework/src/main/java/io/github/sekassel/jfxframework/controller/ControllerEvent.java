package io.github.sekassel.jfxframework.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ControllerEvent {

    /**
     * Methods annotated with this annotation will be upon the initialization of the controller.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface onInit {
    }

    /**
     * Methods annotated with this annotation will be upon rendering the controller.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface onRender {
    }

    /**
     * Methods annotated with this annotation will be upon destroying the controller.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface onDestroy {
    }


}
