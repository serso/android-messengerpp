package org.solovyev.android.messenger.accounts;

import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AccountEvent extends AbstractTypedJEvent<Account, AccountEventType> {

	AccountEvent(@Nonnull Account account, @Nonnull AccountEventType type, @Nullable Object data) {
		super(account, type, data);
	}

	@Nonnull
	public Account getRealm() {
		return getEventObject();
	}
}
