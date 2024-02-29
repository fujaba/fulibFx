package de.uniks.ludo;

import dagger.Module;
import dagger.Provides;
import de.uniks.ludo.service.GameService;

import java.util.Random;

@Module
public class TestModule {

    @Provides
    GameService gameService() {
        return new GameService(new Random(42));
    }

}
