package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.common.listeners.AbstractTypedJEvent;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 8:59 PM
 */
public final class RealmUiEvent extends AbstractTypedJEvent<Realm, RealmUiEventType> {

	public RealmUiEvent(@Nonnull Realm realm, @Nonnull RealmUiEventType type, @Nullable Object data) {
		super(realm, type, data);
	}

	@Nonnull
	public Realm getRealmDef() {
		return getEventObject();
	}

}
