package org.solovyev.android.messenger.accounts.tasks;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.users.User;

import static org.solovyev.android.messenger.App.getUserService;

public class UserSaverCallable implements Callable<User> {

	@Nonnull
	public static final String TASK_NAME = "user-save";

	@Nonnull
	private final Account account;

	@Nonnull
	private final User user;

	public UserSaverCallable(@Nonnull Account account, @Nonnull User user) {
		this.account = account;
		this.user = user;
	}

	@Override
	public User call() {
		final List<User> users = Arrays.<User>asList(user);
		getUserService().mergeUserContacts(account.getUser().getEntity(), users, false, true);
		return user;
	}
}
