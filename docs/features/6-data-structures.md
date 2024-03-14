# Other Data structures
The framework provides a few data structures that can be used to simplify the creation of JavaFX applications.

### Duplicator
A `Duplicator` is a functional interface that takes an object and returns a duplicate of the object. It can be used to
create copies of Objects such as JavaFX nodes. The framework used Duplicators for the For-Loops.
For more information, see the section about [Node duplication](5-node-duplicator.md).

### Subscriber
The `Subscriber` is a utility class that can be used to manage subscriptions. It can be used to subscribe to observables
without having to worry about disposing them one by one. For more information, see the section about [subscriptions](1-subscriber.md).

### RefreshableDisposable
The `RefreshableDisposable` is an interface defining disposables that can be refreshed. Refreshing a disposable will
make it usable again, after it has been disposed. An example for this is the `RefreshableCompositeDisposable`.

### ItemListDisposable
The `ItemListDisposable` will run an action for all added items upon disposal. This can be used to clean up items in a list
with a certain action in a single disposable.

```java
ItemListDisposable<String> disposable = ItemListDisposable.of(item -> print(item), "!", "World");
disposable.add("Hello");
disposable.dispose(); // Will print "Hello", "World" and "!" to the console (LIFO order)
```

### TraversableQueue
A `TraversableQueue` is a queue that saves the latest n entries made to it. Like a normal queue, it can be used to store items in a FIFO order.
When the queue is full, the oldest item will be removed. However, it also provides methods to traverse the queue,
meaning you can go back and forth between items in the queue. When you go back in the queue and add a new item, all
items after the current item will be removed. This can be compared to the history of a browser and is used for the
history of routes.

### Either
The `Either` class combines two optionals into a single object. It used to represent a value with two possible types.
The framework uses it for the history, where the `Either` can be either a controller or a route. It has some methods
for checking which type it is and for getting the value correct value.

---

[⬅ Duplicator](5-node-duplicator.md) | [Overview](README.md) | [Features ➡](README.md)