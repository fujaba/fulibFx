package org.fulib.fx.app.mocking;

import io.reactivex.rxjava3.core.Observable;
import org.fulib.fx.app.mocking.controller.MyMainController;
import org.fulib.fx.app.mocking.controller.MySubComponent;
import org.fulib.fx.app.mocking.service.MyService;
import org.fulib.fx.controller.Subscriber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(MockitoExtension.class)
public class MyMainControllerTest extends ControllerTest {

    @Mock // We want to mock the service completely
    MyService myService;
    @Spy // We only want to override parts of the subcomponent, so we use a spy
    MySubComponent mySubComponent;
    @Spy // We currently don't need to mock the subscriber, so we use a spy
    Subscriber subscriber;

    @InjectMocks // We want to test the main controller
    MyMainController myMainController;

    @Test
    public void test() {

        // We want to mock the service to return a different string
        when(myService.getObservable()).thenReturn(Observable.just("This is a test string."));
        // We want to mock the subcomponent to override the onRender method
        doNothing().when(mySubComponent).onRender();

        MyApp.scheduler().scheduleDirect(() -> stage.requestFocus());
        waitForFxEvents();

        app.show(myMainController); // Show the main controller

        assertNotNull(lookup("This is a test string."));
        assertEquals(List.of(), mySubComponent.getChildren());
    }

}
