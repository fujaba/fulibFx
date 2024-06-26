package org.fulib.fx.duplicate;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.fulib.fx.duplicate.duplicators.impl.*;

import java.util.HashMap;

import static org.fulib.fx.util.FrameworkUtil.error;

/**
 * A registry for {@link Duplicator}s.
 * <p>
 * This class is used to register {@link Duplicator}s for specific classes.
 * <p>
 * Call {@link Duplicators#duplicate(Object)} to duplicate an object.
 * <p>
 * Use {@link Duplicators#register(Class, Duplicator)} to register a {@link Duplicator} for a specific class.
 * You can also override existing {@link Duplicator}s or extend them to create new ones if you need to add additional properties.
 *
 * @see Duplicator
 */
public class Duplicators {

    private static final HashMap<Class<?>, Duplicator<?>> DUPLICATORS = new HashMap<>();

    static {
        register(Button.class, new ButtonDuplicator<>());
        register(HBox.class, new HBoxDuplicator<>());
        register(VBox.class, new VBoxDuplicator<>());
        register(Region.class, new RegionDuplicator<>());
        register(Pane.class, new PaneDuplicator<>());
        register(Label.class, new LabelDuplicator<>());
        register(Text.class, new TextDuplicator<>());
        register(ImageView.class, new ImageViewDuplicator<>());
    }

    public Duplicators() {
        // Prevent instantiation
    }

    /**
     * Registers a {@link Duplicator} for the given class.
     *
     * @param clazz      The class to register the {@link Duplicator} for
     * @param duplicator The {@link Duplicator} to register
     * @param <T>        The type of the class
     */
    public static <T> void register(Class<T> clazz, Duplicator<T> duplicator) {
        DUPLICATORS.put(clazz, duplicator);
    }

    /**
     * Duplicates the given object using the registered {@link Duplicator}.
     *
     * @param object The object to duplicate
     * @param <T>    The type of the object
     * @return The duplicated object
     * @throws IllegalArgumentException If no {@link Duplicator} is registered for the given object
     */
    public static <T> T duplicate(T object) {
        if (object == null) {
            return null;
        }

        if (!DUPLICATORS.containsKey(object.getClass())) {
            throw new IllegalArgumentException(error(9006).formatted(object.getClass()));
        }

        @SuppressWarnings("unchecked")
        Duplicator<T> duplicator = (Duplicator<T>) DUPLICATORS.get(object.getClass());

        return duplicator.duplicate(object);
    }


}
