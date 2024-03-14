# History and refresh

The framework also provides a history of visited controllers. The history acts like a stack and can be
used to go back and forth between previously visited routes. The history is automatically updated when using the `show`
method. The history works like the history of a browser, meaning you can go back and forth again, but after going back
and visiting an alternative route, the routes that were previously in the history will be removed.

The history can be navigated using the `back()` and `forward()` methods of the `FulibFxApp` class.

Using the `refresh()` method of the `FulibFxApp` class, you can refresh the currently displayed controller. This will
destroy the controller and reload it with the same parameters as before. This can be used to update the view of a
controller whilst being in dev mode. Refreshing a controller will run the `onDestroy` method of the controller and then
run the `onInit` and `onRender` methods again.

<img width="640" height="360" src="../assets/hot-reload.gif" alt="Hot reloading">

When being in dev mode, the framework will automatically refresh the controller when the corresponding FXML file is
changed. This can be used to quickly test changes to the view without having to restart the application. In order to
enable the automatic refresh, call `autoRefresher().setup()` in the `start` method of the `FulibFxApp` class and 
provide the path where the FXML files are located. The path should be relative to the root of the project.

---

[⬅ For](2-for.md) | [Overview](README.md) | [Modals ➡](4-modals.md)