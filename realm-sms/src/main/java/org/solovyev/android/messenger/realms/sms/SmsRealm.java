package org.solovyev.android.messenger.realms.sms;

import android.content.Context;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.realms.RealmState;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/27/13
 * Time: 8:43 PM
 */
final class SmsRealm extends AbstractRealm<SmsRealmConfiguration> {

	public SmsRealm(@Nonnull String id, @Nonnull RealmDef realmDef, @Nonnull User user, @Nonnull SmsRealmConfiguration configuration, @Nonnull RealmState state) {
		super(id, realmDef, user, configuration, state);
	}

	@Nonnull
	@Override
	protected RealmConnection newRealmConnection0(@Nonnull Context context) {
		return new SmsRealmConnection(this, context);
	}

	@Nonnull
	@Override
	public String getDisplayName(@Nonnull Context context) {
		return context.getString(getRealmDef().getNameResId());
	}

	@Nonnull
	@Override
	public RealmUserService getRealmUserService() {
		return new SmsRealmUserService(this);
	}

	@Nonnull
	@Override
	public RealmChatService getRealmChatService() {
		return new SmsRealmChatService();
	}
}
