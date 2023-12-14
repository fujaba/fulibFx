package io.github.sekassel.jfxframework.controller;

import dagger.Lazy;
import io.github.sekassel.jfxframework.FxFramework;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

@Singleton
public class AutoRefresher {

    @Inject
    Lazy<FxFramework> framework;

    WatchService watchService;
    Disposable disposable;
    private boolean enabled = false;

    @Inject
    public AutoRefresher() {
    }

    public void setup(String directory) {

        this.enabled = true;

        try {
            this.watchService = FileSystems.getDefault().newWatchService();

            Path path = Paths.get(directory);

            WatchKey key = path.register(watchService, ENTRY_MODIFY);


            disposable = Schedulers.newThread().scheduleDirect(() -> {
                while (enabled) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path file = (Path) event.context();
                        // TODO: Some OS fire multiple events for a single change, so we need to check if the file was already modified
                        if (file.toString().endsWith(".fxml")) {
                            // TODO: Actually refresh the controller
                            System.out.println("File changed: " + file);
                            //framework.get().refresh();
                        }
                    }
                }
            });

        } catch (IOException e) {
            throw new RuntimeException("Couldn't start file service!", e);
        }
    }

    public void close() {
        try {
            enabled = false;
            if (watchService != null) this.watchService.close();
            if (disposable != null) this.disposable.dispose();

        } catch (IOException e) {
            throw new RuntimeException("Couldn't close watcher!", e);
        }
    }

}
