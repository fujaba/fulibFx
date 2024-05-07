# Component List Cells [![Javadocs](https://javadoc.io/badge2/org.fulib/fulibFx/Javadocs.svg?color=green)](https://javadoc.io/doc/org.fulib/fulibFx/latest/org/fulib/fx/constructs/listview/ComponentListCell.html)

Component List Cells can be used to display a subcomponent as a cell in a ListView. Whenever an item is added to or removed from the list, the subcomponent updates accordingly.
The `ComponentListCell` class is a subclass of the `ListCell` class and can be used in the same way. The `ComponentListCell` can be used to set the cell factory of a `ListView` to display a subcomponent for each item in the list.

```java
@Controller
public class MainController {
    
    @FXML
    private ListView<MyItem> listView;
    
    @Inject
    App app;

    @Inject
    MyComponentProvider componentProvider;
    
    @OnRender
    public void render(@ParamsMap Map<String, Object> params) {
        listView.setCellFactory(param -> new ComponentListCell<>(app, componentProvider, params));
    }
    
    @OnDestroy
    public void destroy() {
        listView.getItems().clear(); // Clear the listview to dispose the components
    }
}
```

### ReusableItemComponent [![Javadocs](https://javadoc.io/badge2/org.fulib/fulibFx/Javadocs.svg?color=green)](https://javadoc.io/doc/org.fulib/fulibFx/latest/org/fulib/fx/constructs/listview/ReusableItemComponent.html)
If the component implements the `ReusableItemComponent` interface, the `ComponentListCell` will call the `setItem` method of the component whenever the item changes. This allows the component to update its view based on the new item.
If the component doesn't implement this interface, it will be destroyed and a new component will be created whenever the item changes.

```java
@Component
public class MyComponent extends VBox implements ReusableItemComponent<MyItem> {
    private MyItem item;

    @Override
    public void setItem(MyItem item) {
        this.item = item;
        // Update the view based on the new item
    }
}
```

### Parameters
You can pass parameters to the component by adding them to the `ComponentListCell` constructor.

```java
listView.setCellFactory(param -> new ComponentListCell<>(app, componentProvider, params));
```

When a new component is created, the parameters `item` and `list` are automatically added to the map. The `item` parameter contains the current item of the cell and the `list` parameter contains the list of all items.
If parameters with the same key are already present in the map, they will not be overwritten.

### Disposing
The components created by the `ComponentListCell` are automatically disposed when they are removed from the list. 
Therefore, you have to make sure that the listview is cleared, when the main controller is destroyed.

---

[⬅ Data Structures](6-data-structures.md) | [Overview](README.md)
