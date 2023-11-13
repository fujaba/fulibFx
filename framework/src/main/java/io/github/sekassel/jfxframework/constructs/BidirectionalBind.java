package io.github.sekassel.jfxframework.constructs;

import io.github.sekassel.jfxframework.FxFramework;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;

/**
 * Bidirectional binding of two properties of two objects in FXML.
 * The first object's property will be bound to the second object's property.
 * <p>
 * If one object's property is changed, the other object's property will be changed accordingly.
 * <p>
 * The properties will be received by calling a method in the form of <code>property + "Property"</code> (by default).
 * This can be changed by setting the format.
 * <p>
 * Example for defining a bidirectional binding in FXML:
 * <pre>
 * {@code
 * <TextField fx:id="textField"/>
 * <TextField fx:id="otherTextField"/>
 * <fx:define>
 *     <BidirectionalBind object1="$textField" property1="text" object2="$otherTextField" property2="text"/>
 * </fx:define>
 * }
 * </pre>
 */
public class BidirectionalBind {

    private Object object1;
    private String property1;

    private Object object2;
    private String property2;

    private String format = "%sProperty";

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Object getObject1() {
        return object1;
    }

    public void setObject1(Object object1) {
        this.object1 = object1;
        bind();
    }

    public String getProperty1() {
        return property1;
    }

    public void setProperty1(String property1) {
        this.property1 = property1;
        bind();
    }

    public Object getObject2() {
        return object2;
    }

    public void setObject2(Object object2) {
        this.object2 = object2;
        bind();
    }

    public String getProperty2() {
        return property2;
    }

    public void setProperty2(String property2) {
        this.property2 = property2;
        bind();
    }

    @SuppressWarnings("unchecked")
    private <T> void bind() {
        if (object1 == null || property1 == null || object2 == null || property2 == null) return;
        try {
            Property<T> property1 = (Property<T>) object1.getClass().getMethod(this.property1 + "Property").invoke(object1);
            Property<T> property2 = (Property<T>) object2.getClass().getMethod(this.property2 + "Property").invoke(object2);
            Bindings.bindBidirectional(property1, property2);
        } catch (Exception e) {
            FxFramework.logger().severe(e.getMessage());
        }
        object1 = object2 = null;
        property1 = property2 = null;
    }
}