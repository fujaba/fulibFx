package io.github.sekassel.jfxframework.duplicate;

import io.github.sekassel.jfxframework.duplicate.duplicators.impl.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.HashMap;

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
        register(String.class, String::new);

        register(Button.class, new ButtonDuplicator<>());
        register(HBox.class, new HBoxDuplicator<>());
        register(VBox.class, new VBoxDuplicator<>());
        register(Region.class, new RegionDuplicator<>());
        register(Pane.class, new PaneDuplicator<>());
        register(Label.class, new LabelDuplicator<>());
        register(Text.class, new TextDuplicator<>());
        register(ImageView.class, new ImageViewDuplicator<>());
    }

    public static <T> void register(Class<T> clazz, Duplicator<T> duplicator) {
        DUPLICATORS.put(clazz, duplicator);
    }

    public static <T> T duplicate(T object) {
        if (object == null) {
            return null;
        }

        if (!DUPLICATORS.containsKey(object.getClass())) {
            throw new IllegalArgumentException("No duplicator registered for " + object.getClass());
        }

        @SuppressWarnings("unchecked")
        Duplicator<T> duplicator = (Duplicator<T>) DUPLICATORS.get(object.getClass());

        return duplicator.duplicate(object);
    }


}
