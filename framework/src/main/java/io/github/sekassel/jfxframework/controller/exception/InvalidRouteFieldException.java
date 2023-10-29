package io.github.sekassel.jfxframework.controller.exception;

import java.lang.reflect.Field;

public class InvalidRouteFieldException extends RuntimeException {

    public InvalidRouteFieldException(Field field) {
        super("Field %s in %s is annotated with @Route but is not a Provider<T> providing a controller.".formatted(field.getName(), field.getDeclaringClass().getName()));
    }

}
