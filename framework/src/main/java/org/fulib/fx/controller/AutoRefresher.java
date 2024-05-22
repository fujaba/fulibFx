package org.fulib.fx.controller;

import dagger.Lazy;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.fulib.fx.FulibFxApp;
import org.fulib.fx.util.FileUtil;
import org.fulib.fx.util.FrameworkUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.fulib.fx.util.FrameworkUtil.error;

@Singleton
public class AutoRefresher {

    // The string to look for in fxml files to find the controller class
    private static final String FX_CONTROLLER_STRING = "fx:controller=\"%s\"";

    @Inject
    Lazy<FulibFxApp> framework;

    WatchService watchService;
    Disposable disposable;
    private boolean enabled = false;

    @Inject
    public AutoRefresher() {
    }

    public void setup(Path directory) {

        if (!Files.isDirectory(directory)) {
            throw new RuntimeException(error(9007).formatted(directory));
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
                            if (FileUtil.getContent(file.toFile()).contains(String.format(FX_CONTROLLER_STRING, framework.get().frameworkComponent().router().current().getKey().getClass().getName()))) {
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
            throw new RuntimeException(error(9004), e);
        }
    }

    public void close() {
        try {
            enabled = false;
            if (watchService != null) this.watchService.close();
            if (disposable != null) this.disposable.dispose();
        } catch (IOException e) {
            throw new RuntimeException(error(9005), e);
        }
    }

}
