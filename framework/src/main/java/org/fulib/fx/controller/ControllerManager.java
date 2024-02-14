package org.fulib.fx.controller;

import io.reactivex.rxjava3.disposables.Disposable;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Pair;
import org.fulib.fx.FulibFxApp;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.SubComponent;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.annotation.param.Param;
import org.fulib.fx.annotation.param.Params;
import org.fulib.fx.annotation.param.ParamsMap;
import org.fulib.fx.controller.building.ControllerBuildFactory;
import org.fulib.fx.controller.exception.IllegalControllerException;
import org.fulib.fx.util.Util;
import org.fulib.fx.util.disposable.RefreshableCompositeDisposable;
import org.fulib.fx.util.reflection.Reflection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Manages the initialization, rendering and destroying of controllers.
 * <p>
 * This class is used internally by the framework and should not be used directly.
 */
@Singleton
public class ControllerManager {

    // Map of controllers that have been initialized
    private final RefreshableCompositeDisposable cleanup = new RefreshableCompositeDisposable();

    @Inject
    public ControllerManager() {
    }

    /**
     * Initializes and renders the given controller. Calls the onInit and onRender methods. See {@link #init(Object, Map)} and {@link #render(Object, Map)}.
     * <p>
     * The controller/component instance(s) will be added to the set of initialized controllers and will be destroyed when a new main controller is set.
     *
     * @param instance   The controller instance
     * @param parameters The parameters to pass to the controller
     * @return The rendered controller
     */
    public Parent initAndRender(Object instance, Map<String, Object> parameters) {

        // Initialize the controller
        init(instance, parameters, true);

        // Render the controller
        return render(instance, parameters);
    }


    /**
     * Initializes the given controller/component. Calls the onInit method(s) and recursively initializes all subcomponents.
     * <p>
     * <b>Order:</b> Controller -> Subcomponents -> Subcomponents of subcomponents -> ...
     *
     * @param instance                   The controller/component instance
     * @param parameters                 The parameters to pass to the controller/component
     * @param disposeOnNewMainController Whether the controller/component should be destroyed when a new main controller is set
     * @return A disposable that can be used to destroy the controller/component and all its subcomponents manually
     */
    public Disposable init(@NotNull Object instance, @NotNull Map<@NotNull String, @Nullable Object> parameters, boolean disposeOnNewMainController) {
        Disposable disposable = Disposable.fromRunnable(() -> destroy(instance));

        init(instance, parameters);

        if (disposeOnNewMainController) {
            this.cleanup.add(disposable);
        }

        return disposable;
    }

    /**
     * Initializes the given controller/component.
     * Calls the onInit method(s) and recursively initializes all sub-controllers.
     * <p>
     * All initialized controllers will be added to the list of initialized controllers.
     * If a controller/component is added to the list, all its subcomponents will follow right after it.
     *
     * @param instance   The controller/component instance
     * @param parameters The parameters to pass to the controller
     */
    public static void init(@NotNull Object instance, @NotNull Map<@NotNull String, @Nullable Object> parameters) {

        // Check if the instance is a controller
        if (!Util.isController(instance))
            throw new IllegalControllerException("Class '%s' is not a controller or component.".formatted(instance.getClass().getName()));

        // Inject parameters into the controller fields
        fillParametersIntoFields(instance, parameters);

        // Call parameter setter methods
        callParamMethods(instance, parameters);
        callParamsMethods(instance, parameters);
        callParamsMapMethods(instance, parameters);

        // Call the onInit method(s)
        callMethodsWithAnnotation(instance, onInit.class, parameters);

        // Initialize all sub-controllers
        Reflection.callMethodsForFieldInstances(instance, getSubComponentFields(instance), (subController) -> init(subController, parameters));

    }

    /**
     * Renders the given controller/component instance. Renders all sub-controllers recursively and then calls the onRender method(s) before returning the rendered controller.
     * <p>
     * <b>Important:</b> This method assumes that the controller has already been initialized.
     * The controller will <u>not automatically be destroyed</u> when using only this method.
     * Use {@link #init(Object, Map, boolean)} before, to initialize and automatically destroy the controller or destroy it yourself afterward.
     * <p>
     * If the controller specifies a fxml file in its {@link Controller#view()},
     * it will be loaded and the controller will be set as the controller of the fxml file.
     * <p>
     * If the controller is a component (extends from a JavaFX Parent), the component itself will be rendered and returned.
     * This can be combined with the {@link Component#view()} to set the controller as the root of the fxml file.
     * <p>
     * If the controller specifies a method as {@link Controller#view()}, the method will be called and the returned Parent will be returned.
     * In order to specify a method, the view must start with a '#'. The method must be in the controller class and must return a (subclass of) Parent.
     * Example: {@code @Controller(view = "#getView")} will call the method {@code Parent getView()} in the controller.
     *
     * @param instance   The controller instance
     * @param parameters The parameters to pass to the controller
     * @return The rendered controller/component
     */
    public static Parent render(Object instance, Map<String, Object> parameters) {

        // Check if the instance is a controller/component
        boolean component = instance.getClass().isAnnotationPresent(Component.class) && Util.isComponent(instance);

        if (!component && !instance.getClass().isAnnotationPresent(Controller.class))
            throw new IllegalArgumentException("Class '%s' is not a controller or component.".formatted(instance.getClass().getName()));

        // Render all sub-controllers
        Reflection.callMethodsForFieldInstances(instance, getSubComponentFields(instance), (subController) -> render(subController, parameters));

        // Get the view of the controller
        Parent parent;
        String view = component ?
                instance.getClass().getAnnotation(Component.class).view() :
                instance.getClass().getAnnotation(Controller.class).view();

        // If the controller extends from a javafx Parent, render it
        // This can be combined with the view annotation to set the controller as the root of the fxml file
        if (component) {
            parent = view.isEmpty() ? (Parent) instance : loadFXML(view, instance, true);
        }

        // If the controller specifies a method as view, call it
        else if (view.startsWith("#")) {
            String methodName = view.substring(1);
            try {
                Method method = instance.getClass().getDeclaredMethod(methodName);
                if (!Parent.class.isAssignableFrom(method.getReturnType()))
                    throw new RuntimeException("Method '" + methodName + "()' in class '" + instance.getClass().getName() + "' does not return a Parent.");
                method.setAccessible(true);
                parent = (Parent) method.invoke(instance);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Method '" + methodName + "()' in class '" + instance.getClass().getName() + "' does not exist.");
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("Method '" + methodName + "()' in class '" + instance.getClass().getName() + "' could not be called.", e);
            }
        }

        // If the controller specifies a fxml file, load it. This will also load sub-controllers specified in the FXML file
        else {
            String fxmlPath = view.isEmpty() ? Util.transform(instance.getClass().getSimpleName()) + ".fxml" : view;
            parent = loadFXML(fxmlPath, instance, false);
        }

        // Call the onRender method
        callMethodsWithAnnotation(instance, onRender.class, parameters);

        return parent;
    }

    /**
     * Destroys the given controller/component by calling all methods annotated with {@link onDestroy}.
     * <p>
     * <b>Important:</b> Do not use this method on a controller's view but on the controller itself.
     * <p>
     * If the controller has subcomponents, they will be destroyed first recursively in reverse order.
     * <p>
     * If the controller has an undestroyed Subscriber field, a warning will be logged in development mode.
     *
     * @param instance The controller/component instance to destroy
     */
    public static void destroy(@NotNull Object instance) {
        if (!Util.isController(instance))
            throw new IllegalArgumentException("Class '%s' is not a controller or component.".formatted(instance.getClass().getName()));

        // Destroying should be done in exactly the reverse order of initialization
        List<Field> subComponentFields = new ArrayList<>(getSubComponentFields(instance));
        Collections.reverse(subComponentFields);

        // Destroy all sub-controllers
        Reflection.callMethodsForFieldInstances(instance, subComponentFields, ControllerManager::destroy);

        // Call destroy methods
        callMethodsWithAnnotation(instance, onDestroy.class, Map.of());

        // In development mode, check for undestroyed subscribers
        if (Util.runningInDev()) {
            Reflection.getFieldsOfType(instance.getClass(), Subscriber.class) // Get all Subscriber fields
                    .stream()
                    .map(field -> {
                        try {
                            field.setAccessible(true);
                            return new Pair<>(field, (Subscriber) field.get(instance)); // Get the Subscriber instance, if it exists
                        } catch (IllegalAccessException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(pair -> pair.getKey() != null)
                    .filter(pair -> !pair.getValue().isDisposed()) // Filter out disposed subscribers
                    .forEach(pair ->
                            FulibFxApp.logger().warning("Found undestroyed subscriber '%s' in class '%s'.".formatted(pair.getKey().getName(), instance.getClass().getName()))
                    );
        }
    }

    /**
     * Destroys all controllers that have been initialized and are currently displayed.
     */
    public void cleanup() {
        cleanup.dispose();
        cleanup.refresh();
    }

    /**
     * Loads a fxml file using a custom controller factory.
     * This method is used internally by the framework and should not be used directly.
     * <p>
     * If the fxml file contains an element with a controller class annotated with {@link Controller},
     * an instance provided by the router will be used as the controller for the element.
     *
     * @param fileName The name of the fxml resource file (with path and file extension)
     * @param instance The controller instance to use
     * @return A parent representing the fxml file
     */
    private static @NotNull Parent loadFXML(@NotNull String fileName, @NotNull Object instance, boolean setRoot) {

        URL url = instance.getClass().getResource(fileName);
        if (url == null) {
            String urlPath = instance.getClass().getPackageName().replace(".", "/") + "/" + fileName;
            throw new RuntimeException("Could not find resource '" + urlPath + "'");
        }

        File file = Util.getResourceAsLocalFile(instance.getClass(), fileName);

        // If the file exists, use it instead of the resource (development mode, allows for hot reloading)
        if (file.exists()) {
            try {
                url = file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException("File '" + file.getAbsolutePath() + "' exists, but could not be converted to URL.", e);
            }
        }

        ControllerBuildFactory builderFactory = new ControllerBuildFactory(instance);

        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(c -> instance);
        loader.setBuilderFactory(builderFactory);

        if (setRoot) {
            loader.setRoot(instance);
        }

        try {
            return loader.load();
        } catch (IOException exception) {
            throw new RuntimeException("Couldn't load the FXML file for controller '%s'".formatted(instance.getClass()), exception);
        }
    }

    /**
     * Returns a list of all fields in the given instance that are annotated with {@link SubComponent}.
     *
     * @param instance The instance to get the fields from
     * @return A list of all fields in the given instance that are annotated with {@link SubComponent}
     */
    @Unmodifiable
    private static List<Field> getSubComponentFields(Object instance) {
        return Reflection.getFieldsWithAnnotation(instance.getClass(), SubComponent.class)
                .stream()
                .filter(field -> {
                    if (!field.getType().isAnnotationPresent(Component.class)) {
                        FulibFxApp.logger().warning("Field '%s' in class '%s' is annotated with @SubComponent but is not a subcomponent.".formatted(field.getName(), instance.getClass().getName()));
                        return false;
                    }
                    return true;
                }).toList();
    }

    /**
     * Calls all methods annotated with a certain annotation in the provided controller. The method will be called with the given parameters if they're annotated with @Param or @Params.
     *
     * @param annotation The annotation to look for
     * @param parameters The parameters to pass to the methods
     */
    private static void callMethodsWithAnnotation(@NotNull Object instance, @NotNull Class<? extends Annotation> annotation, @NotNull Map<@NotNull String, @Nullable Object> parameters) {
        for (Method method : Reflection.getMethodsWithAnnotation(instance.getClass(), annotation)) {
            try {
                method.setAccessible(true);
                method.invoke(instance, getApplicableParameters(method, parameters));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Couldn't run method '" + method.getName() + "' annotated with '" + annotation.getName() + "' in '" + instance.getClass().getName() + "'", e);
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
    private static void fillParametersIntoFields(@NotNull Object instance, @NotNull Map<@NotNull String, @Nullable Object> parameters) {
        // Fill the parameters into fields annotated with @Param
        for (Field field : Reflection.getFieldsWithAnnotation(instance.getClass(), Param.class)) {
            try {
                boolean accessible = field.canAccess(instance);
                field.setAccessible(true);

                // If the field is a WriteableValue, use the setValue method
                if (WritableValue.class.isAssignableFrom(field.getType())) {
                    field.get(instance).getClass().getMethod("setValue", Object.class).invoke(field.get(instance), parameters.get(field.getAnnotation(Param.class).value()));
                } else {
                    Object value = parameters.get(field.getAnnotation(Param.class).value());
                    if (value == null) { // If the value is null, we don't need to check the type
                        field.set(instance, null);
                    } else if (Reflection.canBeAssigned(field.getType(), value)) {
                        field.set(instance, value);
                    } else {
                        throw new RuntimeException("Parameter named '" + field.getAnnotation(Param.class).value() + "' in field '" + field.getName() + "' is of type " + field.getType().getName() + " but the provided value is of type " + value.getClass().getName());
                    }
                }

                field.setAccessible(accessible);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Couldn't fill parameter '" + field.getAnnotation(Param.class).value() + "' into field '" + field.getName() + "' in '" + instance.getClass().getName() + "'", e);
            } catch (InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException("Couldn't execute setter method with parameter '" + field.getAnnotation(Param.class).value() + "' for field '" + field.getName() + "' in '" + instance.getClass().getName() + "'", e);
            }
        }

        // Fill the parameters into fields annotated with @ParamsMap
        for (Field field : Reflection.getFieldsWithAnnotation(instance.getClass(), ParamsMap.class)) {

            if (!Util.isMapWithTypes(field, String.class, Object.class)) {
                throw new RuntimeException("Field annotated with @ParamsMap in class '" + instance.getClass().getName() + "' is not of type " + Map.class.getName() + "<String, Object>");
            }

            try {
                boolean accessible = field.canAccess(instance);
                field.setAccessible(true);

                // Check if field is final
                if (Modifier.isFinal(field.getModifiers())) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) field.get(instance);
                    map.putAll(parameters);
                } else {
                    field.set(instance, parameters);
                }
                field.setAccessible(accessible);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Couldn't fill parameters into field '" + field.getName() + "' in '" + instance.getClass().getName() + "'", e);
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
    private static void callParamMethods(Object instance, Map<String, Object> parameters) {
        Reflection.getMethodsWithAnnotation(instance.getClass(), Param.class).forEach(method -> {
            try {
                method.setAccessible(true);
                Object value = parameters.get(method.getAnnotation(Param.class).value());

                if (value == null) {
                    method.invoke(instance, (Object) null);
                    return;
                }

                if (Reflection.canBeAssigned(method.getParameterTypes()[0], value)) {
                    method.invoke(instance, value);
                } else {
                    throw new RuntimeException("Parameter named '" + method.getAnnotation(Param.class).value() + "' in method '" + method.getName() + "' is of type " + method.getParameterTypes()[0].getName() + " but the provided value is of type " + value.getClass().getName());
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Couldn't fill parameter '" + method.getAnnotation(Param.class).value() + "' into method '" + method.getName() + "' in '" + instance.getClass().getName() + "'", e);
            }
        });
    }

    /**
     * Calls all methods annotated with @Params in the given instance with the values of the specified parameters.
     * (Multiple parameter injection into methods)
     *
     * @param instance   The instance to fill the parameters into
     * @param parameters The parameters to fill into the methods
     */
    private static void callParamsMethods(Object instance, Map<String, Object> parameters) {
        Reflection.getMethodsWithAnnotation(instance.getClass(), Params.class).forEach(method -> {
            try {
                method.setAccessible(true);

                String[] paramNames = method.getAnnotation(Params.class).value();
                if (method.getParameters().length != paramNames.length)
                    throw new RuntimeException("Method '" + method.getName() + "' in class '" + instance.getClass().getName() + "' has a different amount of parameters than the provided parameters map (%d != %d)".formatted(method.getParameters().length, paramNames.length));

                Object[] methodParams = new Object[paramNames.length];

                // Fill the parameters into the method
                for (int i = 0; i < paramNames.length; i++) {
                    Object value = parameters.get(paramNames[i]);
                    if (Reflection.canBeAssigned(method.getParameterTypes()[i], value)) {
                        methodParams[i] = value;
                    } else {
                        throw new RuntimeException("Parameter named '" + paramNames[i] + "' in method '" + method.getName() + "' is of type " + method.getParameterTypes()[i].getName() + " but the provided value is of type " + value.getClass().getName());
                    }
                }

                method.invoke(instance, methodParams);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Couldn't fill parameters into method '" + method.getName() + "' in '" + instance.getClass().getName() + "'", e);
            }
        });
    }

    /**
     * Fills the parameters map into the methods annotated with @ParamsMap in the given instance.
     * (Map injection into setters)
     *
     * @param instance   The instance to fill the parameters into
     * @param parameters The parameters to fill into the methods
     */
    private static void callParamsMapMethods(Object instance, Map<String, Object> parameters) {
        Reflection.getMethodsWithAnnotation(instance.getClass(), ParamsMap.class).forEach(method -> {

            if (method.getParameterCount() != 1 || !Util.isMapWithTypes(method.getParameters()[0], String.class, Object.class)) {
                throw new RuntimeException("Method '" + method.getName() + "' in class '" + instance.getClass().getName() + "' annotated with @ParamsMap has to have exactly one parameter of type " + Map.class.getName() + "<String, Object>");
            }

            try {
                method.setAccessible(true);
                method.invoke(instance, parameters);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Couldn't fill parameters into method '" + method.getName() + "' in '" + instance.getClass().getName() + "'", e);
            }
        });
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
    private static @Nullable Object @NotNull [] getApplicableParameters(@NotNull Method method, @NotNull Map<String, Object> parameters) {
        return Arrays.stream(method.getParameters()).map(parameter -> {
            Param param = parameter.getAnnotation(Param.class);
            ParamsMap paramsMap = parameter.getAnnotation(ParamsMap.class);

            if (param != null && paramsMap != null)
                throw new RuntimeException("Parameter '" + parameter.getName() + "' in method '" + method.getDeclaringClass().getName() + "#" + method.getName() + "' is annotated with both @Param and @Params");

            // Check if the parameter is annotated with @Param and if the parameter is of the correct type
            if (param != null) {
                if (parameters.containsKey(param.value()) && !Reflection.canBeAssigned(parameter.getType(), parameters.get(param.value()))) {
                    throw new RuntimeException("Parameter named '" + param.value() + "' in method '" + method.getDeclaringClass().getName() + "#" + method.getName() + "' is of type " + parameter.getType().getName() + " but the provided value is of type " + parameters.get(param.value()).getClass().getName());
                }
                return parameters.get(param.value());
            }

            // Check if the parameter is annotated with @Params and if the parameter is of the type Map<String, Object>
            if (paramsMap != null) {
                if (!Util.isMapWithTypes(parameter, String.class, Object.class)) {
                    throw new RuntimeException("Parameter annotated with @Params in method '" + method.getClass().getName() + "#" + method.getName() + "' is not of type " + Map.class.getName() + "<String, Object>");
                }
                return parameters;
            }
            return null;
        }).toArray();
    }

}
