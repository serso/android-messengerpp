package org.solovyev.android.messenger.realms;

import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 12:48 PM
 */
public final class RealmGuiEvent extends AbstractTypedJEvent<Account, RealmGuiEventType> {

	public RealmGuiEvent(@Nonnull Account account, @Nonnull RealmGuiEventType type, @Nullable Object data) {
		super(account, type, data);
	}

	@Nonnull
	public Account getRealm() {
		return getEventObject();
	}
}
