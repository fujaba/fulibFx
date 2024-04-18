package org.fulib.fx;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.disposables.DisposableContainer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.controller.AutoRefresher;
import org.fulib.fx.dagger.DaggerFrameworkComponent;
import org.fulib.fx.dagger.FrameworkComponent;
import org.fulib.fx.data.Either;
import org.fulib.fx.util.ControllerUtil;
import org.fulib.fx.util.ReflectionUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.logging.Logger;

import static org.fulib.fx.util.FrameworkUtil.error;

public abstract class FulibFxApp extends Application {

    public static final Scheduler FX_SCHEDULER = Schedulers.from(Platform::runLater);
    public static final Logger LOGGER = Logger.getLogger(FulibFxApp.class.getName());

    private static Path resourcesPath = Path.of("src/main/resources");

    // The component holding the required dependencies like router, controller manager, etc.
    private FrameworkComponent frameworkComponent;

    // The stage of the application
    private Stage stage;

    // The title pattern for the application
    private Function<String, String> titlePattern = s -> s;

    // The instance of the current main controller (last controller displayed with show())
    private Object currentMainController;

    /**
     * Returns the path to the 'resources' directory.
     *
     * @return The path to the resources directory
     */
    public static @NotNull Path resourcesPath() {
        return resourcesPath;
    }

    /**
     * Sets the path to the 'resources' directory. If your 'resources' directory differs from "src/main/resources", you can set it here.
     * <p>
     * If your project is contained in another directory (e.g. gradle submodule), you can set the path to the 'resources' directory here.
     *
     * @param path The path to the resources directory (e.g. "example/src/main/resources")
     */
    public static void setResourcesPath(@NotNull Path path) {
        resourcesPath = path;
    }

    /**
     * Initializes and renders the component with the given route.
     * <p>
     * The component has to be destroyed manually, for example by calling {@link #destroy(Node)}.
     *
     * @param route The route of the component to render
     * @param <T>   The type of the component
     * @return The rendered component
     */
    public @NotNull <T extends Node> T initAndRender(@NotNull String route) {
        return initAndRender(route, Map.of());
    }

    /**
     * Initializes and renders a component instance (a controller with the {@link Component} annotation).
     * <p>
     * The component has to be destroyed manually, for example by calling {@link #destroy(Node)}.
     *
     * @param component The component instance
     * @param <T>       The type of the component
     * @return The rendered component
     */
    public @NotNull <T extends Node> T initAndRender(@NotNull T component) {
        return initAndRender(component, Map.of());
    }

    /**
     * Initializes and renders the component with the given route.
     * <p>
     * The component has to be destroyed manually, for example by calling {@link #destroy(Node)}.
     *
     * @param route  The route of the component to render
     * @param params The arguments passed to the component
     * @param <T>    The type of the component
     * @return The rendered component
     */
    public @NotNull <T extends Node> T initAndRender(@NotNull String route, @NotNull Map<@NotNull String, @Nullable Object> params) {
        return initAndRender(route, params, null);
    }

    /**
     * Initializes and renders a component instance (a controller with the {@link Component} annotation).
     * <p>
     * The component has to be destroyed manually, for example by calling {@link #destroy(Node)}.
     *
     * @param component The component instance
     * @param params    The arguments passed to the component
     * @param <T>       The type of the component
     * @return The rendered component
     */
    public @NotNull <T extends Node> T initAndRender(@NotNull T component, @NotNull Map<@NotNull String, @Nullable Object> params) {
        return initAndRender(component, params, null);
    }

    /**
     * Initializes and renders the component with the given route.
     * <p>
     * If a disposable is provided, the disposable will be modified to include the cleanup of the rendered component.
     * The provided disposable can be used to destroy the component with all its children manually.
     * <p>
     * If no disposable is provided, the component has to be destroyed manually, for example by calling {@link #destroy(Node)}.
     *
     * @param route     The route of the component to render
     * @param params    The arguments passed to the component
     * @param onDestroy A disposable which will be modified to include the disposable of the component
     * @param <T>       The type of the component
     * @return The rendered component
     */
    @SuppressWarnings("unchecked")
    public @NotNull <T extends Node> T initAndRender(@NotNull String route, @NotNull Map<@NotNull String, @Nullable Object> params, @Nullable DisposableContainer onDestroy) {
        Object component = this.frameworkComponent.router().getController(route);
        if (!ControllerUtil.isComponent(component)) {
            throw new IllegalArgumentException(error(1000).formatted(component.getClass().getName()));
        }
        return initAndRender((T) component, params, onDestroy);
    }

    /**
     * Initializes and renders a component instance (a controller with the {@link Component} annotation).
     * <p>
     * If a disposable is provided, the disposable will be modified to include the cleanup of the rendered component.
     * The provided disposable can be used to destroy the component with all its children manually.
     * <p>
     * If no disposable is provided, the component has to be destroyed manually, for example by calling {@link #destroy(Node)}.
     *
     * @param component The component instance
     * @param params    The arguments passed to the component
     * @param onDestroy A disposable which will be modified to include the disposable of the component
     * @return The rendered component
     */
    public @NotNull <T extends Node> T initAndRender(@NotNull T component, @NotNull Map<@NotNull String, @Nullable Object> params, @Nullable DisposableContainer onDestroy) {
        if (!ControllerUtil.isComponent(component)) {
            throw new IllegalArgumentException(error(1000).formatted(component.getClass().getName()));
        }
        Disposable disposable = this.frameworkComponent.controllerManager().init(component, params, false);
        if (onDestroy != null) {
            onDestroy.add(disposable);
        }

        @SuppressWarnings("unchecked") // We know that the component will return itself as the view
        T rendered = (T) this.frameworkComponent().controllerManager().render(component, params);
        return rendered;
    }

    /**
     * Destroys a rendered controller extending Parent.
     *
     * @param rendered The rendered controller instance
     * @param <T>      The type of the controller
     * @return The destroyed controller instance
     * @throws IllegalArgumentException If the given instance is not a controller extending Parent
     */
    public @NotNull <T extends Node> T destroy(@NotNull T rendered) {
        this.frameworkComponent().controllerManager().destroy(rendered);
        return rendered;
    }

    @MustBeInvokedByOverriders
    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        this.frameworkComponent = DaggerFrameworkComponent.builder().framework(this).build();

        Scene scene = new Scene(new Pane()); // Show default scene

        this.stage.setScene(scene);
        this.stage.show();
    }

    @Override
    public void stop() {
        cleanup();
        autoRefresher().close();
        System.exit(0);
    }

    /**
     * Initializes, renders and displays a controller.
     *
     * @param route The route of the controller to render
     * @return The rendered parent of the controller
     */
    public @NotNull Parent show(@NotNull String route) {
        return show(route, Map.of());
    }

    /**
     * Initializes, renders and displays a controller.
     *
     * @param controller The controller to render
     * @return The rendered parent of the controller
     */
    public @NotNull Parent show(@NotNull Object controller) {
        return show(controller, Map.of());
    }

    /**
     * Initializes, renders and displays a controller.
     *
     * @param controller The controller to render
     * @param params     The arguments passed to the controller
     * @return The rendered parent of the controller
     */
    public @NotNull Parent show(@NotNull Object controller, @NotNull Map<String, Object> params) {
        // Check if the given instance is a controller
        if (!ControllerUtil.isControllerOrComponent(controller)) {
            throw new IllegalArgumentException(error(1001).formatted(controller.getClass().getName()));
        }
        // Render the new controller and check if it's a parent
        cleanup();
        Node renderedNode = this.frameworkComponent().controllerManager().initAndRender(controller, params);
        if (!(renderedNode instanceof Parent renderedParent)) {
            throw new IllegalArgumentException(error(1011).formatted(controller.getClass().getName()));
        }
        // Add the new controller to the history and display it
        this.frameworkComponent.router().addToHistory(new Pair<>(Either.right(controller), params));
        prepareDisplay(null, renderedParent, controller, params);
        return renderedParent;
    }

    /**
     * Initializes, renders and displays a controller.
     *
     * @param route  The route of the controller to render
     * @param params The arguments passed to the controller
     * @return The rendered parent of the controller
     */
    public @NotNull Parent show(@NotNull String route, @NotNull Map<@NotNull String, @Nullable Object> params) {
        // Get the controller instance and display it (most logic is in renderRoute)
        cleanup();
        Pair<Object, Parent> rendered = this.frameworkComponent.router().renderRoute(route, params);
        prepareDisplay(route, rendered.getValue(), rendered.getKey(), params);
        return rendered.getValue();
    }

    /**
     * Prepares the display of a controller by setting all required properties and calling the onShow method.
     * The controller will be displayed using {@link #display(Parent)}.
     *
     * @param route      The route of the controller to render
     * @param parent     The parent to display
     * @param controller The controller instance
     * @param params     The arguments passed to the controller
     */
    protected void prepareDisplay(@Nullable String route, @NotNull Parent parent, @NotNull Object controller, @NotNull Map<String, Object> params) {
        this.currentMainController = controller;
        getTitle(currentMainController).ifPresent(title -> stage.setTitle(formatTitle(title)));
        display(parent);
        onShow(Optional.ofNullable(route), controller, parent, params);
    }

    /**
     * Displays the given parent. Will be called when showing a main controller or when reloading the currently displayed controller.
     * This method can be overridden to add custom behavior like multiple controllers on top of each other.
     *
     * @param parent The parent to display
     */
    protected void display(@NotNull Parent parent) {
        stage.getScene().setRoot(parent);
    }

    // Internal helper method
    protected void cleanup() {
        this.frameworkComponent.controllerManager().cleanup();
    }

    /**
     * Called when the application shows a new controller.
     * <p>
     * This method is called after the controller is initialized and rendered.
     *
     * @param route      The route of the controller (empty if the controller has been shown directly)
     * @param controller The controller instance
     * @param rendered   The rendered parent of the controller
     * @param params     The arguments passed to the controller
     */
    protected void onShow(Optional<String> route, Object controller, Parent rendered, Map<String, Object> params) {
        // Override this method
    }

    /**
     * Returns the stage of the application.
     *
     * @return The stage of the application
     */
    public Stage stage() {
        return this.stage;
    }

    /**
     * Returns the currently used component of the application.
     * <p>
     * <b>Warning:</b> This method should only be used for internal purposes.
     *
     * @return The component
     */
    @ApiStatus.Internal
    public FrameworkComponent frameworkComponent() {
        return this.frameworkComponent;
    }

    /**
     * Registers all routes in the given class.
     *
     * @param routes The class to register the routes from
     */
    public void registerRoutes(Object routes) {
        this.frameworkComponent.router().registerRoutes(routes);
    }

    /**
     * Sets the default resource bundle to use for FXML files if no resource bundle is provided in the controller/component.
     */
    public void setDefaultResourceBundle(ResourceBundle resourceBundle) {
        this.frameworkComponent.controllerManager().setDefaultResourceBundle(resourceBundle);
    }

    /**
     * Returns auto refresher of the application.
     */
    public AutoRefresher autoRefresher() {
        return this.frameworkComponent.autoRefresher();
    }

    /**
     * Returns to the previous controller in the history if possible.
     *
     * @return True if the application could go back, false otherwise
     */
    public boolean back() {
        return navigate(this.frameworkComponent.router().back());
    }

    /**
     * Forwards to the next controller in the history if possible.
     *
     * @return True if the application could go forward, false otherwise
     */
    public boolean forward() {
        return navigate(this.frameworkComponent.router().forward());
    }

    private boolean navigate(Pair<Object, Node> to) {
        if (to != null) {

            // Check if the controller is a parent (should always be the case except if provoked by the user)
            if (!(to.getValue() instanceof Parent parent)) {
                throw new IllegalArgumentException(error(1011).formatted(to.getKey().getClass().getName()));
            }

            this.currentMainController = to.getKey(); // Set the current controller instance
            display(parent); // Display the controller (already rendered)
            return true;
        }
        return false;
    }

    /**
     * Refreshes the current controller.
     * <p>
     * The controller will be cleaned up and then re-initialized and re-rendered.
     * This method will re-use the current route and parameters and not update the history.
     */
    public void refresh() {
        cleanup();
        Object controller = this.currentMainController; // Get the current controller
        Map<String, Object> params = this.frameworkComponent.router().current().getValue(); // Use the same parameters as before
        this.frameworkComponent.controllerManager().init(controller, params, true); // Re-initialize the controller
        Node node = this.frameworkComponent.controllerManager().render(controller, params); // Re-render the controller
        if (!(node instanceof Parent parent)) {
            throw new IllegalArgumentException(error(1011).formatted(controller.getClass().getName()));
        }
        ReflectionUtil.resetMouseHandler(stage());
        display(parent);
    }

    /**
     * Sets the title pattern for the application.
     * This title pattern expects a function that will be called with the title of the controller and should return the final title.
     *
     * @param titlePattern The title pattern
     */
    public void setTitlePattern(Function<String, String> titlePattern) {
        this.titlePattern = titlePattern;
    }

    /**
     * Sets the title pattern for the application.
     * This title pattern expects a '%s' placeholder which will be replaced with the title of the controller.
     *
     * @param titlePattern The title pattern
     */
    public void setTitlePattern(String titlePattern) {
        this.titlePattern = titlePattern::formatted;
    }

    /**
     * Formats the title of a controller using the title pattern.
     *
     * @param title The title of the controller
     * @return The formatted title
     */
    public String formatTitle(String title) {
        return this.titlePattern.apply(title);
    }

    /**
     * Returns the title of the given controller.
     *
     * @param controller The controller instance
     * @return The title of the controller
     */
    public Optional<String> getTitle(Object controller) {
        return this.frameworkComponent.controllerManager().getTitle(controller);
    }

    /**
     * Sets the history size of the application.
     * <p>
     * The smaller the history size, the less memory is used.
     * <p>
     * The larger the history size, the more controllers can be navigated back and forth.
     * <p>
     * The default history size is 10. It cannot be smaller than 1.
     *
     * @param size The history size
     */
    public void setHistorySize(int size) {
        this.frameworkComponent.router().setHistorySize(size);
    }

}
