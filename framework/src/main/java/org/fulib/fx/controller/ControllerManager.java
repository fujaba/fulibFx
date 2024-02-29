package org.fulib.fx.controller;

import io.reactivex.rxjava3.disposables.Disposable;
import javafx.beans.value.WritableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Pair;
import org.fulib.fx.FulibFxApp;
import org.fulib.fx.annotation.controller.*;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.annotation.param.Param;
import org.fulib.fx.annotation.param.Params;
import org.fulib.fx.annotation.param.ParamsMap;
import org.fulib.fx.controller.building.ControllerBuildFactory;
import org.fulib.fx.controller.exception.IllegalControllerException;
import org.fulib.fx.data.disposable.RefreshableCompositeDisposable;
import org.fulib.fx.util.*;
import org.fulib.fx.util.reflection.Reflection;
import org.jetbrains.annotations.ApiStatus;
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

import static org.fulib.fx.util.FrameworkUtil.error;

/**
 * Manages the initialization, rendering and destroying of controllers.
 * <p>
 * This class is used internally by the framework and should not be used directly.
 */
@Singleton
@ApiStatus.Internal
public class ControllerManager {

    // Map of controllers that have been initialized
    private final RefreshableCompositeDisposable cleanup = new RefreshableCompositeDisposable();

    private static ResourceBundle defaultResourceBundle;

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
     * Calls the onInit method(s) and recursively initializes all subcomponents.
     * <p>
     * All initialized controllers will be added to the list of initialized controllers.
     * If a controller/component is added to the list, all its subcomponents will follow right after it.
     *
     * @param instance   The controller/component instance
     * @param parameters The parameters to pass to the controller
     */
    public void init(@NotNull Object instance, @NotNull Map<@NotNull String, @Nullable Object> parameters) {

        // Check if the instance is a controller
        if (!ControllerUtil.isController(instance))
            throw new IllegalControllerException(error(1001).formatted(instance.getClass().getName()));

        // Inject parameters into the controller fields
        fillParametersIntoFields(instance, parameters);

        // Call parameter setter methods
        callParamMethods(instance, parameters);
        callParamsMethods(instance, parameters);
        callParamsMapMethods(instance, parameters);

        // Call the onInit method(s)
        callMethodsWithAnnotation(instance, onInit.class, parameters);

        // Initialize all subcomponents
        Reflection.callMethodsForFieldInstances(instance, getSubComponentFields(instance), (subController) -> init(subController, parameters));

    }

    /**
     * Renders the given controller/component instance. Renders all subcomponents recursively and then calls the onRender method(s) before returning the rendered controller.
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
    public Parent render(Object instance, Map<String, Object> parameters) {

        // Check if the instance is a controller/component
        boolean component = instance.getClass().isAnnotationPresent(Component.class) && ControllerUtil.isComponent(instance);

        if (!component && !instance.getClass().isAnnotationPresent(Controller.class))
            throw new IllegalArgumentException(error(1001).formatted(instance.getClass().getName()));

        // Render all subcomponents
        Reflection.callMethodsForFieldInstances(instance, getSubComponentFields(instance), (subController) -> render(subController, parameters));

        // Get the view of the controller
        Parent parent;
        String view = component ?
                instance.getClass().getAnnotation(Component.class).view() :
                instance.getClass().getAnnotation(Controller.class).view();

        // If the controller extends from a javafx Parent, render it
        // This can be combined with the view annotation to set the controller as the root of the fxml file
        if (component) {
            if (view.isEmpty()) {
                parent = (Parent) instance;
            } else {
                Parent root = (Parent) instance;
                // Due to the way JavaFX works, we have to clear the children list of the old root before loading its fxml file again
                ReflectionUtil.getChildrenList(instance.getClass(), root).clear();
                parent = loadFXML(view, instance, true);
            }
        }

        // If the controller specifies a method as view, call it
        else if (view.startsWith("#")) {
            String methodName = view.substring(1);
            try {
                Method method = instance.getClass().getDeclaredMethod(methodName);
                if (method.getParameterCount() != 0) {
                    throw new RuntimeException(error(1008).formatted(methodName, instance.getClass().getName()));
                }
                if (!Parent.class.isAssignableFrom(method.getReturnType()))
                    throw new RuntimeException(error(1002).formatted(methodName, instance.getClass().getName()));
                method.setAccessible(true);
                parent = (Parent) method.invoke(instance);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(error(1003).formatted(methodName, instance.getClass().getName()), e);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(error(1004).formatted(methodName, instance.getClass().getName()), e);
            }
        }

        // If the controller specifies a fxml file, load it. This will also load subcomponents specified in the FXML file
        else {
            String fxmlPath = view.isEmpty() ? ControllerUtil.transform(instance.getClass().getSimpleName()) + ".fxml" : view;
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
    public void destroy(@NotNull Object instance) {
        if (!ControllerUtil.isController(instance))
            throw new IllegalArgumentException(error(1001).formatted(instance.getClass().getName()));

        // Destroying should be done in exactly the reverse order of initialization
        List<Field> subComponentFields = new ArrayList<>(getSubComponentFields(instance));
        Collections.reverse(subComponentFields);

        // Destroy all subcomponents
        Reflection.callMethodsForFieldInstances(instance, subComponentFields, this::destroy);

        // Call destroy methods
        callMethodsWithAnnotation(instance, onDestroy.class, Map.of());

        // In development mode, check for undestroyed subscribers
        if (FrameworkUtil.runningInDev()) {
            Reflection.getFieldsOfType(instance.getClass(), Subscriber.class) // Get all Subscriber fields
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
                            FulibFxApp.LOGGER.warning("Found undestroyed subscriber '%s' in class '%s'.".formatted(pair.getKey().getName(), instance.getClass().getName()))
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
    private @NotNull Parent loadFXML(@NotNull String fileName, @NotNull Object instance, boolean setRoot) {

        URL url = instance.getClass().getResource(fileName);
        if (url == null) {
            String urlPath = instance.getClass().getPackageName().replace(".", "/") + "/" + fileName;
            throw new RuntimeException(error(2000).formatted(urlPath));
        }

        File file = FileUtil.getResourceAsLocalFile(FulibFxApp.resourcesPath(), instance.getClass(), fileName);

        // If the file exists, use it instead of the resource (development mode, allows for hot reloading)
        if (file.exists()) {
            try {
                url = file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(error(2001).formatted(file.getAbsolutePath()), e);
            }
        }

        // Set the controller factory and builder factory
        ControllerBuildFactory builderFactory = new ControllerBuildFactory(instance);
        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(c -> instance);
        loader.setBuilderFactory(builderFactory);

        // If the controller has a resource bundle, use it
        ResourceBundle resourceBundle = getResourceBundle(instance);
        if (resourceBundle != null) {
            loader.setResources(resourceBundle);
        }

        // Set the root of the FXML file when a component specifies a view
        if (setRoot) {
            loader.setRoot(instance);
        }

        // Load the FXML file
        try {
            return loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(error(2002).formatted(instance.getClass()), exception);
        }
    }

    /**
     * Returns the resource bundle of the given instance if it has one.
     * If no resource bundle is set, the default resource bundle will be used.
     * If no default resource bundle is set, null will be returned.
     *
     * @param instance The instance to get the resource bundle from
     * @return The resource bundle of the given instance if it has one or the default resource bundle
     * @throws RuntimeException If the instance has more than one field annotated with {@link Resource}
     */
    private static @Nullable ResourceBundle getResourceBundle(@NotNull Object instance) {

        List<Field> fields = Reflection.getFieldsWithAnnotation(instance.getClass(), Resource.class).toList();

        if (fields.isEmpty())
            return defaultResourceBundle;

        if (fields.size() > 1)
            throw new RuntimeException(error(2003).formatted(instance.getClass().getName()));

        return fields
                .stream()
                .filter(field -> {
                    if (field.getType().isAssignableFrom(ResourceBundle.class))
                        return true;
                    throw new RuntimeException(error(2004).formatted(field.getName(), instance.getClass().getName()));
                })
                .map(field -> {
                    try {
                        field.setAccessible(true);
                        return (ResourceBundle) field.get(instance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(error(2005).formatted(field.getName(), instance.getClass().getName()), e);
                    }
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(defaultResourceBundle);
    }

    /**
     * Returns a list of all fields in the given instance that are annotated with {@link SubComponent}.
     *
     * @param instance The instance to get the fields from
     * @return A list of all fields in the given instance that are annotated with {@link SubComponent}
     */
    @Unmodifiable
    private List<Field> getSubComponentFields(Object instance) {
        return Reflection.getFieldsWithAnnotation(instance.getClass(), SubComponent.class)
                .filter(field -> {
                    if (!field.getType().isAnnotationPresent(Component.class)) {
                        FulibFxApp.LOGGER.warning(error(6005).formatted(field.getName(), instance.getClass().getName()));
                        return false;
                    }
                    return true;
                }).toList();
    }

    /**
     * Returns a comparator that compares methods based on the value of the given annotation.
     * The method assumes that the annotation has a method called 'value' that returns an integer value.
     *
     * @param annotation The annotation to compare
     * @return A comparator that compares methods based on the value of the given annotation
     */
    private static Comparator<Method> annotationComparator(@NotNull Class<? extends Annotation> annotation) {
        return (m1, m2) -> {
            Annotation event1 = m1.getAnnotation(annotation);
            Annotation event2 = m2.getAnnotation(annotation);
            try {
                Method value = annotation.getDeclaredMethod("value");
                return Integer.compare((int) value.invoke(event1), (int) value.invoke(event2));
            } catch (ReflectiveOperationException e) {
                return 0;
            }
        };
    }

    /**
     * Calls all methods annotated with a certain annotation in the provided controller. The method will be called with the given parameters if they're annotated with @Param or @Params.
     *
     * @param annotation The annotation to look for
     * @param parameters The parameters to pass to the methods
     */
    private void callMethodsWithAnnotation(@NotNull Object instance, @NotNull Class<? extends Annotation> annotation, @NotNull Map<@NotNull String, @Nullable Object> parameters) {
        for (Method method : Reflection.getMethodsWithAnnotation(instance.getClass(), annotation).sorted(annotationComparator(annotation)).toList()) {
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
        for (Field field : Reflection.getFieldsWithAnnotation(instance.getClass(), Param.class).toList()) {
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
                        throw new RuntimeException(error(4007).formatted(field.getAnnotation(Param.class).value(), field.getName(), instance.getClass().getName(), field.getType().getName(), value.getClass().getName()));
                    }
                }

                field.setAccessible(accessible);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(error(4000).formatted(field.getAnnotation(Param.class).value(), field.getName(), instance.getClass().getName()), e);
            } catch (InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(error(4001).formatted(field.getAnnotation(Param.class).value(), field.getName(), instance.getClass().getName()), e);
            }
        }

        // Fill the parameters into fields annotated with @ParamsMap
        for (Field field : Reflection.getFieldsWithAnnotation(instance.getClass(), ParamsMap.class).toList()) {

            if (!MapUtil.isMapWithTypes(field, String.class, Object.class)) {
                throw new RuntimeException(error(4002).formatted(field.getName(), instance.getClass().getName()));
            }

            try {
                boolean accessible = field.canAccess(instance);
                field.setAccessible(true);

                // Check if field is final
                if (Modifier.isFinal(field.getModifiers())) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) field.get(instance);
                    map.clear();
                    map.putAll(parameters);
                } else {
                    field.set(instance, parameters);
                }
                field.setAccessible(accessible);
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
                    throw new RuntimeException(error(4008).formatted(method.getAnnotation(Param.class).value(), method.getName(), instance.getClass().getName(), method.getParameterTypes()[0].getName(), value.getClass().getName()));
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(error(4005).formatted(method.getAnnotation(Param.class).value(), method.getName(), instance.getClass().getName()), e);
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
    private void callParamsMethods(Object instance, Map<String, Object> parameters) {
        Reflection.getMethodsWithAnnotation(instance.getClass(), Params.class).forEach(method -> {
            try {
                method.setAccessible(true);

                String[] paramNames = method.getAnnotation(Params.class).value();
                if (method.getParameters().length != paramNames.length)
                    throw new RuntimeException(error(4006).formatted(method.getName(), instance.getClass().getName()));

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
        });
    }

    /**
     * Fills the parameters map into the methods annotated with @ParamsMap in the given instance.
     * (Map injection into setters)
     *
     * @param instance   The instance to fill the parameters into
     * @param parameters The parameters to fill into the methods
     */
    private void callParamsMapMethods(Object instance, Map<String, Object> parameters) {
        Reflection.getMethodsWithAnnotation(instance.getClass(), ParamsMap.class).forEach(method -> {

            if (method.getParameterCount() != 1 || !MapUtil.isMapWithTypes(method.getParameters()[0], String.class, Object.class)) {
                throw new RuntimeException(error(4003).formatted(method.getName(), instance.getClass().getName()));
            }

            try {
                method.setAccessible(true);
                method.invoke(instance, parameters);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(error(4010).formatted(method.getName(), instance.getClass().getName()), e);
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
    private @Nullable Object @NotNull [] getApplicableParameters(@NotNull Method method, @NotNull Map<String, Object> parameters) {
        return Arrays.stream(method.getParameters()).map(parameter -> {
            Param param = parameter.getAnnotation(Param.class);
            ParamsMap paramsMap = parameter.getAnnotation(ParamsMap.class);

            if (param != null && paramsMap != null)
                throw new RuntimeException(error(4009).formatted(parameter.getName(), method.getName(), method.getDeclaringClass().getName()));

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
     * Sets the default resource bundle for all controllers that don't have a resource bundle set.
     *
     * @param resourceBundle The default resource bundle
     */
    public void setDefaultResourceBundle(ResourceBundle resourceBundle) {
        defaultResourceBundle = resourceBundle;
    }

    /**
     * Returns the title of the given controller instance if it has one.
     * If the title is a key, the title will be looked up in the resource bundle of the controller.
     *
     * @param instance The controller instance
     * @return The title of the controller
     */
    public Optional<String> getTitle(@NotNull Object instance) {
        if (!instance.getClass().isAnnotationPresent(Title.class))
            return Optional.empty();

        String title = instance.getClass().getAnnotation(Title.class).value();

        if (title.startsWith("%")) {
            title = title.substring(1);
            ResourceBundle resourceBundle = getResourceBundle(instance);
            if (resourceBundle != null) {
                return Optional.of(resourceBundle.getString(title));
            }
            throw new RuntimeException(error(2006).formatted(title, instance.getClass().getName()));
        } else if (title.equals("$name")) {
            return Optional.of(ControllerUtil.transform(instance.getClass().getSimpleName()));
        }

        return Optional.of(title);
    }
}
