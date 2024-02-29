# Keyboard Listener

The framework provides an easy way of registering keybinds for a controller. By using the `@onKey` annotation, you can
define methods that will be called when a key is pressed.

The annotation has multiple parameters for specifying the key or additional keys that have to be pressed.
For more control, the annotated method can also take a `KeyEvent` as a parameter which will be passed to the method when
the key is pressed.

```java
import java.security.Key;

@onKey()
public void onKeyPressed(KeyEvent event) {
    // This method will be called when any key is pressed
    // The KeyEvent can be used to get information about the key that was pressed
}

@onKey(code = KeyCode.ENTER)
public void onEnterPressed() {
    // This method will be called when the enter key is pressed
    // It doesn't matter if shift, ctrl or alt are pressed
}

@onKey(code = KeyCode.P, shift = true)
public void onShiftP() {
    // This method will be called when the shift and p key are pressed
    // Also works with ctrl, alt and meta (e.g. windows key)
}
```

Other parameters that can be used are `type` (e.g. `KEY_PRESSED` or `KEY_RELEASED`) and `target` which specifies where the event should be
captured. The target can be `STAGE` or `SCENE`.

Using `character` and `text` one can access the raw character that was pressed. `character` will be the character that
would result by pressing the key(s) (e.g. SHIFT + 'a' --> 'A') and `text` will be name of the key that was pressed (e.g. "CTRL" for the ctrl key).

See below for an example in action.

```java
@onKey(code = KeyCode.R)
public void rollDice() {
    // ...
}
```

<img width="640" height="360" src="/docs/assets/key-event.gif">