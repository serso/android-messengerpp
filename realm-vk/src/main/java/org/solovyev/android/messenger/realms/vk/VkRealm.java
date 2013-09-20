package org.solovyev.android.messenger.realms.vk;

import android.content.Context;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.realms.AccountState;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.realms.vk.chats.VkAccountChatService;
import org.solovyev.android.messenger.realms.vk.users.VkAccountUserService;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

public final class VkRealm extends AbstractRealm<VkAccountConfiguration> {

	public VkRealm(@Nonnull String id, @Nonnull RealmDef realmDef, @Nonnull User user, @Nonnull VkAccountConfiguration configuration, @Nonnull AccountState state) {
		super(id, realmDef, user, configuration, state);
	}

	@Nonnull
	@Override
	protected RealmConnection newRealmConnection0(@Nonnull Context context) {
		return new VkLongPollRealmConnection(this, context);
	}

	@Nonnull
	@Override
	public String getDisplayName(@Nonnull Context context) {
		final StringBuilder sb = new StringBuilder();

		sb.append(context.getText(getRealmDef().getNameResId()));

		return sb.toString();
	}

	@Nonnull
	@Override
	public AccountUserService getAccountUserService() {
		return new VkAccountUserService(this);
	}

	@Nonnull
	@Override
	public AccountChatService getAccountChatService() {
		return new VkAccountChatService(this);
	}
}
