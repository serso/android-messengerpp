package org.solovyev.android.messenger;

import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.chats.ChatService;

import javax.annotation.Nonnull;

import static org.mockito.Mockito.mock;

public class AppTest {

	@Nonnull
	public static App mockApp() {
		final App app = App.getInstance();
		app.setExceptionHandler(mock(ExceptionHandler.class));
		app.setAccountService(mock(AccountService.class));
		app.setChatService(mock(ChatService.class));
		return app;
	}
}
