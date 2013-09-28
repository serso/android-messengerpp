package org.solovyev.android.messenger;

import android.app.Application;

import javax.annotation.Nonnull;

public class DefaultTestMessengerModule extends AbstractTestMessengerModule {

	public DefaultTestMessengerModule(@Nonnull Application application) {
		super(application);
	}

	@Nonnull
	@Override
	protected MessengerConfiguration newAppConfiguration() {
		return new TestMessengerConfiguration();
	}
}
