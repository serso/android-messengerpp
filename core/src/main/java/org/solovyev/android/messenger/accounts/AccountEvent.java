package org.solovyev.android.messenger.accounts;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.common.listeners.AbstractTypedJEvent;

public final class AccountEvent extends AbstractTypedJEvent<Account, AccountEventType> {

	AccountEvent(@Nonnull Account account, @Nonnull AccountEventType type, @Nullable Object data) {
		super(account, type, data);
	}

	@Nonnull
	public Account getAccount() {
		return getEventObject();
	}
}
