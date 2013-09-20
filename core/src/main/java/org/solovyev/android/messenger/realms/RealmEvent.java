package org.solovyev.android.messenger.realms;

import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class RealmEvent extends AbstractTypedJEvent<Account, RealmEventType> {

	RealmEvent(@Nonnull Account account, @Nonnull RealmEventType type, @Nullable Object data) {
		super(account, type, data);
	}

	@Nonnull
	public Account getRealm() {
		return getEventObject();
	}
}
