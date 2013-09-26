package org.solovyev.android.messenger;

import org.solovyev.android.messenger.accounts.AccountService;

import javax.annotation.Nonnull;

import static org.mockito.Mockito.mock;

public class AppTest {

	@Nonnull
	public static App mockApp() {
		final App app = App.getInstance();
		app.setExceptionHandler(mock(MessengerExceptionHandler.class));
		app.setAccountService(mock(AccountService.class));
		return app;
	}
}
