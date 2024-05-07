package org.fulib.fx.controller.internal;

import javafx.beans.value.WritableValue;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fulib.fx.FulibFxApp;
import org.fulib.fx.annotation.controller.*;
import org.fulib.fx.annotation.event.OnDestroy;
import org.fulib.fx.annotation.event.OnInit;
import org.fulib.fx.annotation.event.OnKey;
import org.fulib.fx.annotation.event.OnRender;
import org.fulib.fx.annotation.param.Param;
import org.fulib.fx.annotation.param.Params;
import org.fulib.fx.annotation.param.ParamsMap;
import org.fulib.fx.controller.ControllerManager;
import org.fulib.fx.util.ControllerUtil;
import org.fulib.fx.util.FrameworkUtil;
import org.fulib.fx.util.KeyEventHolder;
import org.fulib.fx.util.MapUtil;
import org.fulib.fx.util.ReflectionUtil;
import org.fulib.fx.util.reflection.Reflection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.fulib.fx.util.FrameworkUtil.error;

public class ReflectionSidecar<T> implements FxSidecar<T> {
    private final ControllerManager controllerManager;

    private final String title;
    private final Field resourceField;

    private final List<Field> subComponentFields;
    private final List<Field> paramFields;
    private final List<Field> paramsMapFields;
    private final List<Method> paramMethods;
    private final List<Method> paramsMethods;
    private final List<Method> paramsMapMethods;
    private final List<Method> initMethods;
    private final List<Method> renderMethods;
    private final List<Method> destroyMethods;

    public ReflectionSidecar(ControllerManager controllerManager, Class<T> componentClass) {
        this.controllerManager = controllerManager;

        this.title = loadTitle(componentClass);
        this.resourceField = loadResourceField(componentClass);
        this.subComponentFields = loadSubComponentFields(componentClass);
        this.initMethods = ReflectionUtil.getAllNonPrivateMethodsOrThrow(componentClass, OnInit.class)
            .peek(method -> ControllerUtil.checkOverrides(method, OnInit.class))
            .sorted(Comparator.comparingInt(m -> m.getAnnotation(OnInit.class).value()))
            .toList();
        this.renderMethods = ReflectionUtil.getAllNonPrivateMethodsOrThrow(componentClass, OnRender.class)
            .sorted(Comparator.comparingInt(m -> m.getAnnotation(OnRender.class).value()))
            .peek(method -> ControllerUtil.checkOverrides(method, OnRender.class))
            .toList();
        this.destroyMethods = ReflectionUtil.getAllNonPrivateMethodsOrThrow(componentClass, OnDestroy.class)
            .peek(method -> ControllerUtil.checkOverrides(method, OnDestroy.class))
            .sorted(Comparator.comparingInt(m -> m.getAnnotation(OnDestroy.class).value()))
            .toList();

        this.paramFields = ReflectionUtil.getAllNonPrivateFieldsOrThrow(componentClass, Param.class).toList();
        this.paramsMapFields = ReflectionUtil.getAllNonPrivateFieldsOrThrow(componentClass, ParamsMap.class)
            .peek(field -> {
                if (!MapUtil.isMapWithTypes(field, String.class, Object.class)) {
                    throw new RuntimeException(error(4002).formatted(field.getName(), componentClass.getName()));
                }
            })
            .toList();

        this.paramMethods = ReflectionUtil.getAllNonPrivateMethodsOrThrow(componentClass, Param.class).toList();
        this.paramsMethods = ReflectionUtil.getAllNonPrivateMethodsOrThrow(componentClass, Params.class).toList();
        this.paramsMapMethods = ReflectionUtil.getAllNonPrivateMethodsOrThrow(componentClass, ParamsMap.class)
            .peek(method -> {
                if (method.getParameterCount() != 1 || !MapUtil.isMapWithTypes(method.getParameters()[0], String.class, Object.class)) {
                    throw new RuntimeException(error(4003).formatted(method.getName(), componentClass.getName()));
                }
            })
            .toList();
    }

    @Override
    public void init(T instance, Map<String, Object> params) {
        // Inject parameters into the controller fields
        fillParametersIntoFields(instance, params);

        // Call parameter setter methods
        callParamMethods(instance, params);
        callParamsMethods(instance, params);
        callParamsMapMethods(instance, params);

        callMethodsWithAnnotation(instance, params, initMethods, OnInit.class);

        Reflection.callMethodsForFieldInstances(instance, subComponentFields, (subController) -> controllerManager.init(subController, params));
    }

    private void callMethodsWithAnnotation(
        @NotNull Object instance,
        @NotNull Map<@NotNull String, @Nullable Object> parameters,
        List<Method> methods,
        @NotNull Class<? extends Annotation> annotation
    ) {
        for (Method method : methods) {
            try {
                method.setAccessible(true);
                method.invoke(instance, getApplicableParameters(method, parameters));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(error(1005).formatted(method.getName(), annotation.getName(), instance.getClass().getName()), e);
            }
        }
    }

    /**
     * Fills the parameters map into the fields annotated with @Param and @ParamsMap in the given instance.
     * (Parameter/Map injection into fields)
     *
     * @param instance   The instance to fill the parameters into
     * @param parameters The parameters to fill into the fields
     */
    private void fillParametersIntoFields(@NotNull Object instance, @NotNull Map<@NotNull String, @Nullable Object> parameters) {
        // Fill the parameters into fields annotated with @Param
        for (Field field : paramFields) {
            Param paramAnnotation = field.getAnnotation(Param.class);
            String param = paramAnnotation.value();

            // Don't fill the parameter if it's not present (field will not be overwritten, "default value")
            if (!parameters.containsKey(param)) {
                continue;
            }

            Class<?> fieldType = field.getType();
            try {
                field.setAccessible(true);

                Object value = parameters.get(param);
                Object fieldValue = field.get(instance);

                // If the field is a WriteableValue, use the setValue method
                if (WritableValue.class.isAssignableFrom(fieldType) && !(value instanceof WritableValue)) {

                    // We cannot call setValue on a non-existing property
                    if (fieldValue == null) {
                        throw new RuntimeException(error(4001).formatted(param, field.getName(), instance.getClass().getName()));
                    }

                    try {
                        ((WritableValue<Object>) field.get(instance)).setValue(value);
                    } catch (ClassCastException e) {
                        throw new RuntimeException(error(4007).formatted(param, field.getName(), instance.getClass().getName(), fieldType.getName(), value == null ? "null" : value.getClass().getName()));
                    }
                }

                // If not, set the field's value directly
                else if (value == null) {
                    // If the value is null and the field is a primitive, throw an error
                    if (fieldType.isPrimitive()) {
                        throw new RuntimeException(error(4007).formatted(param, field.getName(), instance.getClass().getName(), fieldType.getName(), "null"));
                    }
                    field.set(instance, null); // If the value is null and the field is not a primitive, no type check is necessary
                } else if (Reflection.canBeAssigned(fieldType, value)) {
                    field.set(instance, value); // If the value is not null, we need a type check (respects primitive types)
                } else {
                    throw new RuntimeException(error(4007).formatted(param, field.getName(), instance.getClass().getName(), fieldType.getName(), value.getClass().getName()));
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException(error(4000).formatted(param, field.getName(), instance.getClass().getName()), e);
            }
        }

        // Fill the parameters into fields annotated with @ParamsMap
        for (Field field : paramsMapFields) {
            try {
                field.setAccessible(true);

                // If the map is final, clear it and put all parameters into it
                if (Modifier.isFinal(field.getModifiers())) {
                    Map<String, Object> map = (Map<String, Object>) field.get(instance);
                    map.clear();
                    map.putAll(parameters);
                } else {
                    field.set(instance, parameters);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(error(4010).formatted(field.getName(), instance.getClass().getName()), e);
            }
        }
    }

    /**
     * Calls all methods annotated with @Param in the given instance with the values of the specified parameter.
     * (Parameter injection into setters)
     *
     * @param instance   The instance to fill the parameters into
     * @param parameters The parameters to fill into the methods
     */
    private void callParamMethods(Object instance, Map<String, Object> parameters) {
        for (Method method : paramMethods) {
            try {
                method.setAccessible(true);
                Object value = parameters.get(method.getAnnotation(Param.class).value());

                if (value == null) {
                    method.invoke(instance, (Object) null);
                    continue;
                }

                if (Reflection.canBeAssigned(method.getParameterTypes()[0], value)) {
                    method.invoke(instance, value);
                } else {
                    throw new RuntimeException(error(4008).formatted(method.getAnnotation(Param.class).value(), method.getName(), instance.getClass().getName(), method.getParameterTypes()[0].getName(), value.getClass().getName()));
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(error(4005).formatted(method.getAnnotation(Param.class).value(), method.getName(), instance.getClass().getName()), e);
            }
        }
    }

    /**
     * Calls all methods annotated with @Params in the given instance with the values of the specified parameters.
     * (Multiple parameter injection into methods)
     *
     * @param instance   The instance to fill the parameters into
     * @param parameters The parameters to fill into the methods
     */
    private void callParamsMethods(Object instance, Map<String, Object> parameters) {
        for (Method method : paramsMethods) {
            try {
                method.setAccessible(true);

                String[] paramNames = method.getAnnotation(Params.class).value();
                if (method.getParameters().length != paramNames.length) {
                    throw new RuntimeException(error(4006).formatted(method.getName(), instance.getClass().getName()));
                }

                Object[] methodParams = new Object[paramNames.length];

                // Fill the parameters into the method
                for (int i = 0; i < paramNames.length; i++) {
                    Object value = parameters.get(paramNames[i]);
                    if (Reflection.canBeAssigned(method.getParameterTypes()[i], value)) {
                        methodParams[i] = value;
                    } else {
                        throw new RuntimeException(error(4008).formatted(paramNames[i], method.getName(), instance.getClass().getName(), method.getParameterTypes()[i].getName(), value.getClass().getName()));
                    }
                }

                method.invoke(instance, methodParams);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(error(4011).formatted(method.getName(), instance.getClass().getName()), e);
            }
        }
    }

    /**
     * Fills the parameters map into the methods annotated with @ParamsMap in the given instance.
     * (Map injection into setters)
     *
     * @param instance   The instance to fill the parameters into
     * @param parameters The parameters to fill into the methods
     */
    private void callParamsMapMethods(Object instance, Map<String, Object> parameters) {
        for (Method method : paramsMapMethods) {
            try {
                method.setAccessible(true);
                method.invoke(instance, parameters);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(error(4010).formatted(method.getName(), instance.getClass().getName()), e);
            }
        }
    }

    /**
     * Returns an array with all parameters that are applicable to the given method in the correct order.
     * <p>
     * If the method has a parameter annotated with @Param, the value of the parameter with the same key as the annotation will be used.
     * <p>
     * If the method has a parameter annotated with @ParamsMap, the whole parameters map will be used.
     *
     * @param method     The method to check
     * @param parameters The values of the parameters
     * @return An array with all applicable parameters
     */
    private @Nullable Object @NotNull [] getApplicableParameters(@NotNull Method method, @NotNull Map<String, Object> parameters) {
        return Arrays.stream(method.getParameters()).map(parameter -> {
            Param param = parameter.getAnnotation(Param.class);
            ParamsMap paramsMap = parameter.getAnnotation(ParamsMap.class);

            if (param != null && paramsMap != null) {
                throw new RuntimeException(error(4009).formatted(parameter.getName(), method.getName(), method.getDeclaringClass().getName()));
            }

            // Check if the parameter is annotated with @Param and if the parameter is of the correct type
            if (param != null) {
                if (parameters.containsKey(param.value()) && !Reflection.canBeAssigned(parameter.getType(), parameters.get(param.value()))) {
                    throw new RuntimeException(error(4008).formatted(param.value(), method.getName(), method.getDeclaringClass().getName(), parameter.getType().getName(), parameters.get(param.value()).getClass().getName()));
                }
                return parameters.get(param.value());
            }

            // Check if the parameter is annotated with @Params and if the parameter is of the type Map<String, Object>
            if (paramsMap != null) {
                if (!MapUtil.isMapWithTypes(parameter, String.class, Object.class)) {
                    throw new RuntimeException(error(4004).formatted(parameter.getName(), method.getName(), method.getDeclaringClass().getName()));
                }
                return parameters;
            }
            return null;
        }).toArray();
    }


    /**
     * Returns a list of all fields in the given class that are annotated with {@link SubComponent}.
     *
     * @param componentClass The class to get the fields from
     * @return A list of all fields in the given class that are annotated with {@link SubComponent}
     */
    @Unmodifiable
    private List<Field> loadSubComponentFields(Class<T> componentClass) {
        return ReflectionUtil.getAllNonPrivateFieldsOrThrow(componentClass, SubComponent.class)
            .filter(field -> {
                if (ControllerUtil.isComponent(field.getType())) {
                    return true;
                }

                if (!ControllerUtil.canProvideSubComponent(field)) {
                    FulibFxApp.LOGGER.warning(error(6005).formatted(field.getName(), componentClass.getName()));
                }
                return false;
            }).toList();
    }

    @Override
    public Node render(T instance, Map<String, Object> params) {
        // Render all subcomponents
        Reflection.callMethodsForFieldInstances(instance, subComponentFields, (subController) -> controllerManager.render(subController, params));

        // Get the view of the controller
        final boolean component = ControllerUtil.isComponent(instance);
        final Node node = renderNode(instance, component);

        callMethodsWithAnnotation(instance, params, renderMethods, OnRender.class);

        registerKeyEvents(instance);

        return node;
    }

    private Node renderNode(T instance, boolean component) {
        Node node;
        String view = component ?
            instance.getClass().getAnnotation(Component.class).view() :
            instance.getClass().getAnnotation(Controller.class).view();

        // If the controller extends from a javafx Node, render it
        // This can be combined with the view annotation to set the controller as the root of the fxml file
        if (component) {
            if (view.isEmpty()) {
                node = (Node) instance;
            } else {
                Node root = (Node) instance;
                // Due to the way JavaFX works, we have to clear the children list of the old root before loading its fxml file again
                if (root instanceof Parent parent) {
                    ReflectionUtil.getChildrenList(instance.getClass(), parent).clear();
                }
                node = controllerManager.loadFXML(view, instance, true);
            }
        }

        // If the controller specifies a method returning a parent as its view, call it
        else if (view.startsWith("#")) {
            String methodName = view.substring(1);
            try {
                Method method = instance.getClass().getDeclaredMethod(methodName);
                if (method.getParameterCount() != 0) {
                    throw new RuntimeException(error(1008).formatted(methodName, instance.getClass().getName()));
                }
                if (!Parent.class.isAssignableFrom(method.getReturnType())) {
                    throw new RuntimeException(error(1002).formatted(methodName, instance.getClass().getName()));
                }
                method.setAccessible(true);
                node = (Parent) method.invoke(instance);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(error(1003).formatted(methodName, instance.getClass().getName()), e);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(error(1004).formatted(methodName, instance.getClass().getName()), e);
            }
        }

        // If the controller specifies a fxml file, load it. This will also load subcomponents specified in the FXML file
        else {
            String fxmlPath = view.isEmpty() ? ControllerUtil.transform(instance.getClass().getSimpleName()) + ".fxml" : view;
            node = controllerManager.loadFXML(fxmlPath, instance, false);
        }
        return node;
    }

    /**
     * Registers all key events for the given controller instance.
     *
     * @param instance The controller instance
     */
    private void registerKeyEvents(Object instance) {
        ReflectionUtil.getAllNonPrivateMethodsOrThrow(instance.getClass(), OnKey.class).forEach(method -> {

            ControllerUtil.checkOverrides(method, OnKey.class);

            OnKey annotation = method.getAnnotation(OnKey.class);
            EventType<KeyEvent> type = annotation.type().asEventType();
            EventHandler<KeyEvent> handler = createKeyEventHandler(method, instance, annotation);

            controllerManager.addKeyEventHandler(instance, annotation.target(), type, handler);
        });
    }

    /**
     * Creates an event handler for the given method and instance that will be called when the specified key event occurs.
     *
     * @param method     The method to call
     * @param instance   The instance to call the method on
     * @param annotation The annotation with the key event information
     * @return An event handler for the given method and instance
     */
    private EventHandler<KeyEvent> createKeyEventHandler(Method method, Object instance, OnKey annotation) {
        boolean hasEventParameter = method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(KeyEvent.class);

        if (!hasEventParameter && method.getParameterCount() != 0) {
            throw new RuntimeException(error(1010).formatted(method.getName(), instance.getClass().getName()));
        }

        method.setAccessible(true);

        return event -> {
            if (keyEventMatchesAnnotation(event, annotation)) {
                try {
                    if (hasEventParameter) {
                        method.invoke(instance, event);
                    } else {
                        method.invoke(instance);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(error(1005).formatted(method.getName(), annotation.getClass().getSimpleName(), method.getClass()), e);
                }
            }
        };
    }

    private boolean keyEventMatchesAnnotation(KeyEvent event, OnKey annotation) {
        return (annotation.code() == KeyCode.UNDEFINED || event.getCode() == annotation.code()) &&
            (annotation.character().isEmpty() || event.getCharacter().equals(annotation.character())) &&
            (annotation.text().isEmpty() || event.getText().equals(annotation.text())) &&
            (event.isShiftDown() || !annotation.shift()) &&
            (event.isControlDown() || !annotation.control()) &&
            (event.isAltDown() || !annotation.alt()) &&
            (event.isMetaDown() || !annotation.meta());
    }

    @Override
    public void destroy(T instance) {
        // Destroying should be done in exactly the reverse order of initialization
        List<Field> subComponentFields = new ArrayList<>(this.subComponentFields);
        Collections.reverse(subComponentFields);

        // Destroy all subcomponents
        Reflection.callMethodsForFieldInstances(instance, subComponentFields, controllerManager::destroy);

        // Call destroy methods
        callMethodsWithAnnotation(instance, Map.of(), destroyMethods, OnDestroy.class);
    }

    private @Nullable Field loadResourceField(Class<T> componentClass) {
        List<Field> fields = ReflectionUtil.getAllNonPrivateFieldsOrThrow(componentClass, Resource.class).toList();

        if (fields.isEmpty()) {
            return null;
        }

        if (fields.size() > 1) {
            throw new RuntimeException(error(2003).formatted(componentClass.getName()));
        }

        final Field field = fields.get(0);
        if (!field.getType().isAssignableFrom(ResourceBundle.class)) {
            throw new RuntimeException(error(2004).formatted(field.getName(), componentClass.getName()));
        }

        field.setAccessible(true);
        return field;
    }

    @Override
    public @Nullable ResourceBundle getResources(T instance) {
        if (resourceField == null) {
            return controllerManager.getDefaultResourceBundle();
        }

        try {
            return (ResourceBundle) resourceField.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(error(2005).formatted(resourceField.getName(), instance.getClass().getName()), e);
        }
    }

    private String loadTitle(Class<T> componentClass) {
        if (!componentClass.isAnnotationPresent(Title.class)) {
            return null;
        }
        final String title = componentClass.getAnnotation(Title.class).value();
        if ("$name".equals(title)) {
            return ControllerUtil.transform(componentClass.getSimpleName());
        }
        return title;
    }

    @Override
    public @Nullable String getTitle(T instance) {
        if (title == null || !title.startsWith("%")) {
            return title;
        }

        // This is done at runtime, because the resource bundle might be modified.
        final ResourceBundle resourceBundle = getResources(instance);
        if (resourceBundle == null) {
            throw new RuntimeException(error(2006).formatted(title, instance.getClass().getName()));
        }
        return resourceBundle.getString(title.substring(1));
    }
}
