package org.fulib.fx.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

public class MapUtil {

    /**
     * Returns the key for the given value in the given map.
     *
     * @param map   The map to search in
     * @param value The value to search for
     * @param <T>   The type of the key
     * @param <E>   The type of the value
     * @return The key for the given value in the given map or null if the value is not in the map
     */
    public static <T, E> @Nullable T keyForValue(@NotNull Map<@NotNull T, @NotNull E> map, @NotNull E value) {
        Map.Entry<T, E> keyEntry = map.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), value)).findFirst().orElse(null);
        return keyEntry == null ? null : keyEntry.getKey();
    }

    /**
     * Inserts the given key and value into the given map if the key is not already present or is null.
     * If the key is already present (not null), the value will not be inserted.
     * <p>
     * If the key is already present, the value for the key will be returned.
     *
     * @param map   The map to insert the key and value into
     * @param key   The key to insert
     * @param value The value to insert
     * @param <T>   The type of the key
     * @param <E>   The type of the value
     * @return The value for the key if the key is already present, the new value otherwise
     */
    public static <T, E> @NotNull E putIfNull(@NotNull Map<@NotNull T, @NotNull E> map, @NotNull T key, @NotNull E value) {
        if (map.containsKey(key) && map.get(key) != null) return map.get(key);
        map.put(key, value);
        return value;
    }


    /**
     * Checks if the given parameter is a map with the given key and value types.
     * <p>
     * This will not work for maps not directly specifying the generic types, such as MyMap extends HashMap<Key, Value>.
     *
     * @param parameter The parameter to check
     * @param key       The key type
     * @param value     The value type
     * @return True if the parameter is a valid map field with the given key and value types
     */
    public static boolean isMapWithTypes(@NotNull Parameter parameter, @NotNull Class<?> key, @NotNull Class<?> value) {
        if (Map.class.isAssignableFrom(parameter.getType())) {
            Type genericType = parameter.getParameterizedType();
            return isMapWithTypes(genericType, key, value);
        }
        return false;
    }

    /**
     * Checks if the given field is a map with the given key and value types.
     * <p>
     * This will not work for maps not directly specifying the generic types, such as MyMap extends HashMap<Key, Value>.
     *
     * @param field The field to check
     * @param key   The key type
     * @param value The value type
     * @return True if the parameter is a valid map field with the given key and value types
     */
    public static boolean isMapWithTypes(@NotNull Field field, @NotNull Class<?> key, @NotNull Class<?> value) {
        if (Map.class.isAssignableFrom(field.getType())) {
            Type genericType = field.getGenericType();
            return isMapWithTypes(genericType, key, value);
        }
        return false;
    }

    private static boolean isMapWithTypes(@NotNull Type type, @NotNull Class<?> key, @NotNull Class<?> value) {
        if (type instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            if (typeArguments.length == 2 && typeArguments[0] instanceof Class<?> genericKey && typeArguments[1] instanceof Class<?> genericValue) {
                return genericKey == key && genericValue == value;
            }
        }
        return false;
    }

}
