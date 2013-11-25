/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.*;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.security.base64.ABase64StringEncoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.solovyev.android.messenger.App.newSubTag;
import static org.solovyev.android.messenger.entities.Entities.newEntity;
import static org.solovyev.android.messenger.realms.xmpp.XmppRealm.TAG;
import static org.solovyev.android.messenger.users.Users.newUser;
import static org.solovyev.android.properties.Properties.newProperty;

class XmppAccountUserService extends AbstractXmppRealmService implements AccountUserService {

	@Nonnull
	private final UserService userService;

	XmppAccountUserService(@Nonnull XmppAccount realm, @Nonnull XmppConnectionAware connectionAware, @Nonnull UserService userService) {
		super(realm, connectionAware);
		this.userService = userService;
	}

	@Nonnull
	private List<User> getOnlineUsers(@Nonnull Connection connection, @Nonnull Account account) {
		final List<User> result = new ArrayList<User>();

		final Roster roster = connection.getRoster();
		for (RosterEntry entry : roster.getEntries()) {
			final Presence presence = roster.getPresence(entry.getUser());
			if (presence.isAvailable()) {
				final User user = userService.getUserById(account.newUserEntity(entry.getUser()));
				result.add(user.cloneWithNewStatus(true));
			}
		}

		return result;
	}

	public static void logUserPresence(@Nonnull String subTag, @Nonnull Account account, boolean online, @Nonnull String userName) {
		if (online) {
			Log.d(newSubTag(TAG, subTag), "User online: " + userName + " in account: " + account.getUser().getDisplayName());
		} else {
			Log.d(newSubTag(TAG, subTag), "User offline: " + userName + " in account: " + account.getUser().getDisplayName());
		}
	}

	private static boolean isUserOnline(@Nonnull Account account, @Nonnull Roster roster, @Nonnull final Entity entity) {
		final boolean online;
		if (account.isAccountUser(entity)) {
			// realm user => always online
			online = true;
		} else {
			final RosterEntry entry = roster.getEntry(entity.getAccountEntityId());
			if (entry != null) {
				final Presence presence = roster.getPresence(entry.getUser());
				online = presence.isAvailable();
			} else {
				online = false;
			}
		}
		return online;
	}

	@Nullable
	@Override
	public User getUserById(@Nonnull final String accountUserId) throws AccountConnectionException {
		return doOnConnection(new UserLoader(getAccount(), accountUserId));
	}

	@Nonnull
	@Override
	public List<User> getUserContacts() throws AccountConnectionException {
		return doOnConnection(new UserContactsLoader(getAccount()));
	}

	@Nonnull
	@Override
	public List<User> getOnlineUsers() throws AccountConnectionException {
		return doOnConnection(new OnlineUsersGetter(getAccount()));
	}

	@Nonnull
	@Override
	public User saveUser(@Nonnull User user) {
		return user;
	}

    /*
	**********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

	private static class UserLoader implements XmppConnectedCallable<User> {

		@Nonnull
		private final Account account;

		@Nonnull
		private final String accountUserId;

		public UserLoader(@Nonnull Account account, @Nonnull String accountUserId) {
			this.account = account;
			this.accountUserId = accountUserId;
		}

		@Override
		public User call(@Nonnull Connection connection) throws AccountConnectionException, XMPPException {
			final User result;

			if (account.isAccountUser(accountUserId)) {
				// realm user cannot be found in roster ->  information should be loaded separately
				result = toAccountUser(account.getId(), accountUserId, null, connection);
			} else {
				// try to find user contacts in roster
				final RosterEntry entry = connection.getRoster().getEntry(accountUserId);
				if (entry != null) {
					result = toUser(account.getId(), entry.getUser(), entry.getName(), connection, account);
				} else {
					result = null;
				}
			}

			return result;
		}
	}

	@Nonnull
	public static User toUser(@Nonnull String accountId, @Nonnull String accountUserId, @Nullable String name, @Nonnull Connection connection, @Nonnull Account account) throws XMPPException {
		final Entity entity = newEntity(accountId, accountUserId);
		final List<AProperty> properties = loadUserProperties(true, accountUserId, connection, name);
		final MutableUser user = newUser(entity, properties);
		user.setOnline(isUserOnline(account, connection.getRoster(), entity));
		return user;
	}

	@Nonnull
	public static MutableUser toAccountUser(@Nonnull String accountId, @Nonnull String accountUserId, @Nullable String name, @Nonnull Connection connection) throws XMPPException {
		final Entity entity = newEntity(accountId, accountUserId);
		final List<AProperty> properties = loadUserProperties(true, accountUserId, connection, name);
		final MutableUser user = newUser(entity, properties);
		user.setOnline(true);
		return user;
	}

	@Nonnull
	private static List<AProperty> loadUserProperties(boolean loadVCard,
													  @Nonnull String accountUserId,
													  @Nonnull Connection connection,
													  @Nullable String name) throws XMPPException {
		final List<AProperty> result = new ArrayList<AProperty>();

		if (loadVCard) {
			try {

				final VCard userCard = new VCard();

				userCard.load(connection, accountUserId);

				result.add(newProperty(User.PROPERTY_FIRST_NAME, userCard.getFirstName()));
				result.add(newProperty(User.PROPERTY_LAST_NAME, userCard.getLastName()));
				result.add(newProperty(User.PROPERTY_NICKNAME, userCard.getNickName()));
				result.add(newProperty(User.PROPERTY_EMAIL, userCard.getEmailHome()));
				result.add(newProperty(User.PROPERTY_PHONE, userCard.getPhoneHome("VOICE")));
				result.add(newProperty(XmppRealm.USER_PROPERTY_AVATAR_HASH, userCard.getAvatarHash()));

				final byte[] avatar = userCard.getAvatar();
				if (avatar != null) {
					result.add(newProperty(XmppRealm.USER_PROPERTY_AVATAR_BASE64, ABase64StringEncoder.getInstance().convert(avatar)));
				}

				// full name
				final String fullName = userCard.getField("FN");
				Users.tryParseNameProperties(result, fullName);
			} catch (XMPPException e) {
				// For some reason vcard loading may return timeout exception => investigate this behaviour
				// NOTE: pidgin loads user information also very slow
				Log.w(TAG, e.getMessage(), e);
			}
		} else {
			Users.tryParseNameProperties(result, name);
		}

		return result;
	}

	private static class UserContactsLoader implements XmppConnectedCallable<List<User>> {

		@Nonnull
		private final Account account;

		private UserContactsLoader(@Nonnull Account account) {
			this.account = account;
		}

		@Override
		public List<User> call(@Nonnull final Connection connection) throws AccountConnectionException, XMPPException {
			final Roster roster = connection.getRoster();
			final Collection<RosterEntry> entries = roster.getEntries();

			final List<User> result = new ArrayList<User>(entries.size());
			for (RosterEntry entry : entries) {
				result.add(toUser(account.getId(), entry.getUser(), entry.getName(), connection, account));
			}

			return result;
		}
	}

	private class OnlineUsersGetter implements XmppConnectedCallable<List<User>> {

		@Nonnull
		private final Account account;

		public OnlineUsersGetter(@Nonnull Account account) {
			this.account = account;
		}

		@Override
		public List<User> call(@Nonnull Connection connection) throws AccountConnectionException, XMPPException {
			return getOnlineUsers(connection, account);
		}
	}
}
