package org.solovyev.android.messenger.realms;

import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class RealmEvent extends AbstractTypedJEvent<Realm, RealmEventType> {

	RealmEvent(@Nonnull Realm realm, @Nonnull RealmEventType type, @Nullable Object data) {
		super(realm, type, data);
	}

	@Nonnull
	public Realm getRealm() {
		return getEventObject();
	}
}
