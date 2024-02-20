package org.fulib.fx.util;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Utility class containing different helper methods for the framework or the user.
 */
public class ReflectionUtil {

    private ReflectionUtil() {
        // Prevent instantiation
    }

    /**
     * Returns the class provided by the given provider field.
     *
     * @param providerField The provider field
     * @return The class provided by the provider field or null if the field is not a valid provider field
     */
    public static @Nullable Class<?> getProvidedClass(@NotNull Field providerField) {
        if (providerField.getType() == Provider.class) {
            Type genericType = providerField.getGenericType();

            if (genericType instanceof ParameterizedType parameterizedType) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();

                if (typeArguments.length == 1 && typeArguments[0] instanceof Class<?> genericClass) {
                    return genericClass;
                }
            }
        }
        return null;
    }

    /**
     * Returns the children list of the given parent. This method is used to access the children list of a Parent class
     * even though the method is not public in the Parent class. This should be used with caution and is mainly used
     * for duplicating nodes.
     *
     * @param clazz  The class to get the children list from
     * @param parent The parent to get the children list from
     * @return The children list of the given parent
     */
    @SuppressWarnings("unchecked")
    public static ObservableList<Node> getChildrenList(Class<?> clazz, Parent parent) {
        try {
            Method getChildren = clazz.getDeclaredMethod("getChildren");
            getChildren.setAccessible(true);
            Object childrenList = getChildren.invoke(parent);
            return (ObservableList<Node>) childrenList;
        } catch (Exception e) {
            if (clazz.getSuperclass() == Object.class)
                throw new RuntimeException("Couldn't access getChildren() method in class or superclass", e);
            return getChildrenList(clazz.getSuperclass(), parent);
        }
    }

    /**
     * Returns an instance provided by the given provider field.
     *
     * @param provider The provider field
     * @param instance The instance to get the provider from
     * @return The instance provided by the provider field
     */
    public static Object getInstanceOfProviderField(Field provider, Object instance) {
        try {
            provider.setAccessible(true);
            Provider<?> providerInstance = (Provider<?>) provider.get(instance);
            if (providerInstance == null)
                throw new RuntimeException("Field '" + provider.getName() + "' in '" + provider.getDeclaringClass().getName() + "' is not initialized.");
            return providerInstance.get();
        } catch (NullPointerException e) {
            throw new RuntimeException("Field '" + provider.getName() + "' in '" + provider.getDeclaringClass().getName() + "' is not initialized.");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access field '" + provider.getName() + "' in '" + provider.getDeclaringClass().getName() + "'.", e);
        }
    }

}
