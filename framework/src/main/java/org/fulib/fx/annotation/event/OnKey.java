package org.fulib.fx.annotation.event;

import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods annotated with this annotation will be called upon a key event.
 * The key event can be specified by the parameters of this annotation.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnKey {

    /**
     * The code of the key event.
     *
     * @return The code of the key event
     */
    KeyCode code() default KeyCode.UNDEFINED;

    /**
     * The character of the key event.
     *
     * @return The character of the key event
     */
    String character() default "\0";

    /**
     * The text of the key event.
     *
     * @return The text of the key event
     */
    String text() default "";

    /**
     * Whether the shift key has to be pressed (see {@link OnKey#strict()}).
     *
     * @return Whether the shift key has to be pressed
     */
    boolean shift() default false;

    /**
     * Whether the control key has to be pressed (see {@link OnKey#strict()}).
     *
     * @return Whether the control key has to be pressed
     */
    boolean control() default false;

    /**
     * Whether the alt key has to be pressed (see {@link OnKey#strict()}).
     *
     * @return Whether the alt key has to be pressed
     */
    boolean alt() default false;

    /**
     * Whether the meta key has to be pressed (see {@link OnKey#strict()}).
     * The meta key is the command key on Mac and the control key on other platforms.
     *
     * @return Whether the meta key has to be pressed
     */
    boolean meta() default false;


    /**
     * Whether the settings for meta, control, alt and shift keys are strict.
     * <ul>
     * <li> When strict mode is enabled, setting any of these options to false means the key must not be pressed.</li>
     * <li> When strict mode is disabled, setting any of these options to false means the key does not need to be pressed.</li>
     * </ul>
     *
     * @return Whether the settings for meta, control, alt and shift keys are strict.
     */
    boolean strict() default false;

    /**
     * The target at which the event should be handled.
     *
     * <ul>
     * <li> STAGE: The event will be handled by the stage</li>
     * <li> SCENE: The event will be handled by the scene</li>
     * </ul>
     *
     * @return The target at which the event should be handled
     */
    Target target() default Target.STAGE;

    /**
     * The type of the event.
     *
     * @return The type of the event
     */
    Type type() default Type.PRESSED;

    enum Target {
        STAGE, SCENE
    }

    enum Type {
        ANY(KeyEvent.ANY),
        PRESSED(KeyEvent.KEY_PRESSED),
        RELEASED(KeyEvent.KEY_RELEASED),
        TYPED(KeyEvent.KEY_TYPED);

        private final EventType<KeyEvent> type;

        Type(EventType<KeyEvent> type) {
            this.type = type;
        }

        public EventType<KeyEvent> asEventType() {
            return type;
        }
    }
}
