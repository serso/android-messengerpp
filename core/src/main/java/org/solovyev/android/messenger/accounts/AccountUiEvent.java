package org.solovyev.android.messenger.accounts;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.common.listeners.AbstractTypedJEvent;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 12:48 PM
 */
public final class AccountUiEvent extends AbstractTypedJEvent<Account, AccountUiEventType> {

	public AccountUiEvent(@Nonnull Account account, @Nonnull AccountUiEventType type, @Nullable Object data) {
		super(account, type, data);
	}

	@Nonnull
	public Account getRealm() {
		return getEventObject();
	}
}
