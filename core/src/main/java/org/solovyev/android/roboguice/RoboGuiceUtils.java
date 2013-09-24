package org.solovyev.android.roboguice;

import android.content.Context;
import roboguice.RoboGuice;
import roboguice.inject.ContextScope;

import javax.annotation.Nonnull;

import com.google.inject.Provider;

/**
 * User: serso
 * Date: 8/13/12
 * Time: 1:31 AM
 */
public final class RoboGuiceUtils {

	private RoboGuiceUtils() {
		throw new AssertionError("Not intended for instantiation!");
	}

	public static void runInContextScope(@Nonnull Context context, @Nonnull Runnable runnable) {
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

	@Nonnull
	public static <T> T getInContextScope(@Nonnull Context context, @Nonnull Provider<T> provider) {
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
