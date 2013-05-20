package org.solovyev.android.messenger.realms;

import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 12:48 PM
 */
public final class RealmGuiEvent extends AbstractTypedJEvent<Realm, RealmGuiEventType> {

	public RealmGuiEvent(@Nonnull Realm realm, @Nonnull RealmGuiEventType type, @Nullable Object data) {
		super(realm, type, data);
	}

	@Nonnull
	public Realm getRealm() {
		return getEventObject();
	}
}
