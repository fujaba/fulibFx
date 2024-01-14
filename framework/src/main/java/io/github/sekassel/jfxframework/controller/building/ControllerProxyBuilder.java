/*
 * Copyright (c) 2010, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package io.github.sekassel.jfxframework.controller.building;

import com.sun.javafx.fxml.BeanAdapter;
import com.sun.javafx.fxml.ModuleHelper;
import com.sun.javafx.reflect.ReflectUtil;
import javafx.util.Builder;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * This class is a copy of the ControllerProxyBuilder from the JavaFX framework.
 * It is used to load controllers from FXML files and support the injection of subcontrollers, e.g. provided by Dagger.
 * <p>
 * This builder is strongly modified to support the injection of subcontrollers.
 * Besides the modification of the instance creation, various parts have been optimized and simplified with new Java features.
 */
public class ControllerProxyBuilder<T> extends AbstractMap<String, Object> implements Builder<T> {

    private static final String SETTER_PREFIX = "set";
    private static final String GETTER_PREFIX = "get";

    private final ControllerBuildFactory buildFactory;
    private final Class<?> type;

    private final Map<String, Property> propertiesMap;
    private final Map<String, Object> userValues = new HashMap<>();
    private final Map<String, Object> containers = new HashMap<>();
    private Set<String> propertyNames;

    public ControllerProxyBuilder(ControllerBuildFactory factory, Class<?> tp) {
        this.type = tp;
        this.buildFactory = factory;
        propertiesMap = scanForSetters();
    }

    private static HashMap<String, LinkedList<Method>> getClassMethodCache(Class<?> type) {
        HashMap<String, LinkedList<Method>> classMethodCache = new HashMap<>();

        ReflectUtil.checkPackageAccess(type);

        Method[] declaredMethods = type.getMethods();
        for (Method method : declaredMethods) {
            int modifiers = method.getModifiers();

            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
                String name = method.getName();
                LinkedList<Method> namedMethods = classMethodCache.computeIfAbsent(name, k -> new LinkedList<>());

                namedMethods.add(method);
            }
        }

        return classMethodCache;
    }

    // Utility method for converting list to array via reflection
    // it assumes that localType is array
    private static Object[] convertListToArray(Object userValue, Class<?> localType) {
        Class<?> arrayType = localType.getComponentType();
        List<?> l = BeanAdapter.coerce(userValue, List.class);

        return l.toArray((Object[]) Array.newInstance(arrayType, 0));
    }

    @Override
    public Object put(String key, Object value) {
        userValues.put(key, value);
        return null; // to behave the same way as ObjectBuilder does
    }

    /**
     * This is used to support read-only collection property. This method must
     * return a Collection of the appropriate type if 1. the property is
     * read-only, and 2. the property is a collection. It must return null
     * otherwise.
     */
    private Object getTemporaryContainer(String propName) {
        return containers.computeIfAbsent(propName, this::getReadOnlyProperty);
    }

    // This is used to support read-only collection property.
    private Object getReadOnlyProperty(String propName) {
        // return ArrayListWrapper now and convert it to proper type later
        // during the build - once we know which constructor we will use
        // and what types it accepts
        return new ArrayListWrapper<>();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Set<Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(Object key) {
        return (getTemporaryContainer(key.toString()) != null);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(Object key) {
        return getTemporaryContainer(key.toString());
    }

    @Override
    public T build() {

        // adding collection properties to userValues
        this.putAll(containers);
        propertyNames = userValues.keySet();

        Object retObj = createObjectFromDefaultConstructor();
        if (retObj != null) {
            return (T) retObj;
        }

        throw new RuntimeException("Cannot create instance of "
                + type.getCanonicalName() + " with given set of properties: "
                + userValues.keySet());
    }

    private Object createObjectFromDefaultConstructor() throws RuntimeException {
        Object retObj = createInstance();
        for (String propName : propertyNames) {
            try {
                Property property = propertiesMap.get(propName);
                property.invoke(retObj, getUserValue(propName, property.getType()));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return retObj;
    }

    private Object getUserValue(String key, Class<?> type) {
        Object val = userValues.get(key);
        if (val == null) {
            return null;
        }

        if (type.isAssignableFrom(val.getClass())) {
            return val;
        }

        // we currently don't have proper support for arrays in FXML, so we use lists instead
        // the user provides us with a list and here we convert it to array to pass to the constructor
        if (type.isArray()) {
            try {
                return convertListToArray(val, type);
            } catch (RuntimeException ex) {
                // conversion failed, maybe the ArrayListWrapper is used for storing single value
            }
        }

        if (ArrayListWrapper.class.equals(val.getClass())) {
            // user given value is an ArrayList but the constructor doesn't
            // accept an ArrayList so the ArrayList comes from
            // the getTemporaryContainer method
            // we take the first argument
            List<?> l = (List<?>) val;
            return l.get(0);
        }

        return val;
    }

    /**
     * Creates an instance of the controller class.
     *
     * @return The instance of the controller class
     */
    private Object createInstance() {
        ReflectUtil.checkPackageAccess(type);

        return this.buildFactory.getProvidedInstance(type); // Get the subcontroller instance
    }

    private Map<String, Property> scanForSetters() {
        Map<String, Property> strsMap = new HashMap<>();
        Map<String, LinkedList<Method>> methods = getClassMethodCache(type);

        for (String methodName : methods.keySet()) {
            if (methodName.startsWith(SETTER_PREFIX) && methodName.length() > SETTER_PREFIX.length()) {
                String propName = methodName.substring(SETTER_PREFIX.length());
                propName = Character.toLowerCase(propName.charAt(0)) + propName.substring(1);
                List<Method> methodsList = methods.get(methodName);
                for (Method m : methodsList) {
                    Class<?> retType = m.getReturnType();
                    Class<?>[] argType = m.getParameterTypes();
                    if (retType.equals(Void.TYPE) && argType.length == 1) {
                        strsMap.put(propName, new Setter(m, argType[0]));
                    }
                }
            }
            if (methodName.startsWith(GETTER_PREFIX) && methodName.length() > GETTER_PREFIX.length()) {
                String propName = methodName.substring(GETTER_PREFIX.length());
                propName = Character.toLowerCase(propName.charAt(0)) + propName.substring(1);
                List<Method> methodsList = methods.get(methodName);
                for (Method m : methodsList) {
                    Class<?> retType = m.getReturnType();
                    Class<?>[] argType = m.getParameterTypes();
                    if (Collection.class.isAssignableFrom(retType) && argType.length == 0) {
                        strsMap.put(propName, new Getter(m, retType));
                    }
                }
            }
        }

        return strsMap;
    }

    // Wrapper for ArrayList which we use to store read-only collection properties in
    private static class ArrayListWrapper<T> extends ArrayList<T> {

    }

    private static abstract class Property {
        protected final Method method;
        protected final Class<?> type;

        public Property(Method m, Class<?> t) {
            method = m;
            type = t;
        }

        public Class<?> getType() {
            return type;
        }

        public abstract void invoke(Object obj, Object argStr) throws Exception;
    }

    private static class Setter extends Property {

        public Setter(Method m, Class<?> t) {
            super(m, t);
        }

        @Override
        public void invoke(Object obj, Object argStr) throws Exception {
            Object[] arg = new Object[]{BeanAdapter.coerce(argStr, type)};
            ModuleHelper.invoke(method, obj, arg);
        }
    }

    private static class Getter extends Property {

        public Getter(Method m, Class<?> t) {
            super(m, t);
        }

        @Override
        public void invoke(Object obj, Object argStr) throws Exception {
            // we know that this.method returns collection otherwise it wouldn't be here
            Collection to = (Collection) ModuleHelper.invoke(method, obj, new Object[]{});
            if (argStr instanceof Collection from) {
                to.addAll(from);
            } else {
                to.add(argStr);
            }
        }
    }

}
