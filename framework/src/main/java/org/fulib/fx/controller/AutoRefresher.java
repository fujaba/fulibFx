package org.fulib.fx.controller;

import dagger.Lazy;
import org.fulib.fx.FulibFxApp;
import org.fulib.fx.util.Constants;
import org.fulib.fx.util.Util;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

@Singleton
public class AutoRefresher {

    @Inject
    Lazy<FulibFxApp> framework;

    WatchService watchService;
    Disposable disposable;
    private boolean enabled = false;

    @Inject
    public AutoRefresher() {
    }

    public void setup(Path directory) {

        if (!Util.runningInDev()) {
            FulibFxApp.LOGGER.warning("AutoRefresher is only meant to be used in development mode! Not starting.");
            return;
        }

        this.enabled = true;

        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            WatchKey key = directory.register(watchService, ENTRY_MODIFY);

            AtomicLong lastModified = new AtomicLong(-1);

            disposable = Schedulers.newThread().scheduleDirect(() -> {
                while (enabled) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path file = directory.resolve((Path) event.context());

                        // Some OSs fire multiple events for the same file, so we need to filter them out
                        if (file.toFile().lastModified() == lastModified.get()) {
                            continue;
                        }

                        lastModified.set(file.toFile().lastModified());

                        // Check if the file is a fxml file (not 100% accurate, but good enough)
                        if (file.getFileName().toString().contains(".fxml")) {
                            // Check if the file contains the current main controller as fx:controller (only reload if the fxml file is actually used)
                            if (Util.getContent(file.toFile()).contains(String.format(Constants.FX_CONTROLLER_STRING, framework.get().currentMainController().getClass().getName()))) {
                                FulibFxApp.FX_SCHEDULER.scheduleDirect(() -> {
                                    FulibFxApp.LOGGER.info("Reloading " + file.getFileName() + " because it was modified.");
                                    framework.get().refresh();
                                });
                            }
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
