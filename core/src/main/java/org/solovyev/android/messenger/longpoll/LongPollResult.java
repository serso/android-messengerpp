package org.solovyev.android.messenger.longpoll;

import org.solovyev.android.messenger.realms.Account;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
