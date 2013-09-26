package org.solovyev.android.messenger.realms;

import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 8:59 PM
 */
public final class RealmUiEvent extends AbstractTypedJEvent<Realm, RealmUiEventType> {

	RealmUiEvent(@Nonnull Realm realm, @Nonnull RealmUiEventType type, @Nullable Object data) {
		super(realm, type, data);
	}

	@Nonnull
	public Realm getRealmDef() {
		return getEventObject();
	}

}
