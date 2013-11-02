package org.solovyev.android.messenger;

import android.app.Application;

import javax.annotation.Nonnull;

public class DefaultTestModule extends AbstractTestModule {

	public DefaultTestModule(@Nonnull Application application) {
		super(application);
	}

	@Nonnull
	@Override
	protected Class<? extends Configuration> getConfigurationClass() {
		return TestConfiguration.class;
	}
}
