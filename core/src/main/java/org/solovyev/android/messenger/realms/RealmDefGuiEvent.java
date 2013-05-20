package org.solovyev.android.messenger.realms;

import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 8:59 PM
 */
public final class RealmDefGuiEvent extends AbstractTypedJEvent<RealmDef, RealmDefGuiEventType> {

	public RealmDefGuiEvent(@Nonnull RealmDef realmDef, @Nonnull RealmDefGuiEventType type, @Nullable Object data) {
		super(realmDef, type, data);
	}

	@Nonnull
	public RealmDef getRealmDef() {
		return getEventObject();
	}

}
