package org.solovyev.android.messenger;

import android.app.Application;

import javax.annotation.Nonnull;

public class XmppTestModule extends DefaultTestMessengerModule {

	public XmppTestModule(@Nonnull Application application) {
		super(application);
	}

	@Nonnull
	@Override
	protected Configuration newAppConfiguration() {
		return new XmppTestConfiguration();
	}
}
