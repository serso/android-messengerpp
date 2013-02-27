package org.solovyev.android.messenger;

import android.app.Application;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.xtremelabs.robolectric.Robolectric;
import roboguice.RoboGuice;
import roboguice.config.DefaultRoboModule;
import roboguice.inject.RoboInjector;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestMessengerModule extends MessengerModule {

    private final Map<Class<?>, Object> bindings = new HashMap<Class<?>, Object>();

    @Override
    protected void configure() {
        super.configure();

        //bind(Activity.class).toProvider(Provider.class).in(ContextSingleton.class);
        Set<Map.Entry<Class<?>, Object>> entries = bindings.entrySet();
        for (Map.Entry<Class<?>, Object> entry : entries) {
            bind((Class<Object>) entry.getKey()).toInstance(entry.getValue());
        }
    }

    public void addBinding(Class<?> type, Object object) {
        bindings.put(type, object);
    }

    public static void setUp(Object testObject, TestMessengerModule module) {
        Module roboGuiceModule = RoboGuice.newDefaultRoboModule(Robolectric.application);
        Module testModule = Modules.override(roboGuiceModule).with(module);
        RoboGuice.setBaseApplicationInjector(Robolectric.application, RoboGuice.DEFAULT_STAGE, testModule);
        RoboInjector injector = RoboGuice.getInjector(Robolectric.application);
        injector.injectMembers(testObject);
    }

    public static void tearDown() {
        RoboGuice.util.reset();
        Application app = Robolectric.application;
        DefaultRoboModule defaultModule = RoboGuice.newDefaultRoboModule(app);
        RoboGuice.setBaseApplicationInjector(app, RoboGuice.DEFAULT_STAGE, defaultModule);
    }
}
