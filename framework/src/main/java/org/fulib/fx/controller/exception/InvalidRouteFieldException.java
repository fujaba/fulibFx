package org.fulib.fx.controller.exception;

import java.lang.reflect.Field;

import static org.fulib.fx.util.FrameworkUtil.error;

public class InvalidRouteFieldException extends RuntimeException {

    public InvalidRouteFieldException(Field field) {
        super(error(3003).formatted(field.getName(), field.getDeclaringClass().getName()));
    }

}
