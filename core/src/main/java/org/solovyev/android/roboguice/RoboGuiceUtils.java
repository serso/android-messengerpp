package org.solovyev.android.roboguice;

import android.content.Context;
import com.google.inject.Provider;
import org.jetbrains.annotations.NotNull;
import roboguice.RoboGuice;
import roboguice.inject.ContextScope;

/**
 * User: serso
 * Date: 8/13/12
 * Time: 1:31 AM
 */
public final class RoboGuiceUtils {

    private RoboGuiceUtils() {
        throw new AssertionError("Not intended for instantiation!");
    }

    public static void runInContextScope(@NotNull Context context, @NotNull Runnable runnable) {
        ContextScope contextScope = null;
        try {
            contextScope = RoboGuice.getInjector(context).getInstance(ContextScope.class);
            contextScope.enter(context);
            runnable.run();
        } finally {
            if (contextScope != null) {
                contextScope.exit(context);
            }
        }
    }

    @NotNull
    public static <T> T getInContextScope(@NotNull Context context, @NotNull Provider<T> provider) {
        ContextScope contextScope = null;
        try {
            contextScope = RoboGuice.getInjector(context).getInstance(ContextScope.class);
            contextScope.enter(context);
            return provider.get();
        } finally {
            if (contextScope != null) {
                contextScope.exit(context);
            }
        }
    }
}
