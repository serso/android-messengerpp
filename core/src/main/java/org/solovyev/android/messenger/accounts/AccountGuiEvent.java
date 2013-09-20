package org.solovyev.android.messenger.accounts;

import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 12:48 PM
 */
public final class AccountGuiEvent extends AbstractTypedJEvent<Account, AccountGuiEventType> {

	public AccountGuiEvent(@Nonnull Account account, @Nonnull AccountGuiEventType type, @Nullable Object data) {
		super(account, type, data);
	}

	@Nonnull
	public Account getRealm() {
		return getEventObject();
	}
}
