package org.solovyev.android.messenger.longpoll;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 6/24/12
 * Time: 12:18 AM
 */
public interface LongPollResult {

	@Nullable
	Object updateLongPollServerData(@Nullable Object longPollServerData);

	void doUpdates(@Nonnull User user, @Nonnull Account account);
}
