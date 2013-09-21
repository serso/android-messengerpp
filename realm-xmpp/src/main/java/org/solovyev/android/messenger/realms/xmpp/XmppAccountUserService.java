package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.Properties;
import org.solovyev.android.security.base64.ABase64StringEncoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 8:45 PM
 */
class XmppAccountUserService extends AbstractXmppRealmService implements AccountUserService {

	@Nonnull
	private static final String TAG = "M++/" + XmppAccountUserService.class.getSimpleName();

	XmppAccountUserService(@Nonnull XmppAccount realm, @Nonnull XmppConnectionAware connectionAware) {
		super(realm, connectionAware);
	}

	@Nullable
	@Override
	public User getUserById(@Nonnull final String realmUserId) throws AccountConnectionException {
		return doOnConnection(new UserLoader(getAccount(), realmUserId));
	}

	@Nonnull
	@Override
	public List<User> getUserContacts(@Nonnull final String realmUserId) throws AccountConnectionException {
		return doOnConnection(new UserContactsLoader(getAccount(), realmUserId));
	}

	@Nonnull
	@Override
	public List<User> checkOnlineUsers(@Nonnull final List<User> users) throws AccountConnectionException {
		return doOnConnection(new OnlineUsersChecker(getAccount(), users));
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
		private final String realmUserId;

		public UserLoader(@Nonnull Account account, @Nonnull String realmUserId) {
			this.account = account;
			this.realmUserId = realmUserId;
		}

		@Override
		public User call(@Nonnull Connection connection) throws AccountConnectionException, XMPPException {
			final User result;

			if (account.getUser().getEntity().getRealmEntityId().equals(realmUserId)) {
				// realm user cannot be found in roster ->  information should be loaded separately
				result = toUser(account.getId(), realmUserId, null, true, connection);
			} else {
				// try to find user contacts in roster
				final RosterEntry entry = connection.getRoster().getEntry(realmUserId);
				if (entry != null) {
					result = toUser(account.getId(), entry.getUser(), entry.getName(), false, connection);
				} else {
					result = null;
				}
			}

			return result;
		}
	}

	@Nonnull
	public static User toUser(@Nonnull String realmId, @Nonnull String realmUserId, @Nullable String name, boolean available, @Nonnull Connection connection) throws XMPPException {
		final List<AProperty> properties = loadUserProperties(true, realmUserId, available, connection, name);
		return Users.newUser(EntityImpl.newInstance(realmId, realmUserId), Users.newNeverSyncedUserSyncData(), properties);
	}

	@Nonnull
	private static List<AProperty> loadUserProperties(boolean loadVCard,
													  @Nonnull String realmUserId,
													  boolean available,
													  @Nonnull Connection connection,
													  @Nullable String name) throws XMPPException {
		final List<AProperty> result = new ArrayList<AProperty>();

		result.add(Properties.newProperty(User.PROPERTY_ONLINE, String.valueOf(available)));

		if (loadVCard) {
			try {

				final VCard userCard = new VCard();

				userCard.load(connection, realmUserId);

				result.add(Properties.newProperty(User.PROPERTY_FIRST_NAME, userCard.getFirstName()));
				result.add(Properties.newProperty(User.PROPERTY_LAST_NAME, userCard.getLastName()));
				result.add(Properties.newProperty(User.PROPERTY_NICKNAME, userCard.getNickName()));
				result.add(Properties.newProperty(User.PROPERTY_EMAIL, userCard.getEmailHome()));
				result.add(Properties.newProperty(User.PROPERTY_PHONE, userCard.getPhoneHome("VOICE")));
				result.add(Properties.newProperty(XmppRealm.USER_PROPERTY_AVATAR_HASH, userCard.getAvatarHash()));

				final byte[] avatar = userCard.getAvatar();
				if (avatar != null) {
					result.add(Properties.newProperty(XmppRealm.USER_PROPERTY_AVATAR_BASE64, ABase64StringEncoder.getInstance().convert(avatar)));
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

		@Nonnull
		private final String realmUserId;

		private UserContactsLoader(@Nonnull Account account, @Nonnull String realmUserId) {
			this.account = account;
			this.realmUserId = realmUserId;
		}

		@Override
		public List<User> call(@Nonnull final Connection connection) throws AccountConnectionException, XMPPException {

			if (account.getUser().getEntity().getRealmEntityId().equals(realmUserId)) {
				// realm user => load contacts through the roster
				final Collection<RosterEntry> entries = connection.getRoster().getEntries();

				final List<User> result = new ArrayList<User>(entries.size());
				for (RosterEntry entry : entries) {
					result.add(toUser(account.getId(), entry.getUser(), entry.getName(), false, connection));
				}

				return result;
			} else {
				// we cannot load contacts for contacts in xmpp
				return Collections.emptyList();
			}

		}
	}

	private static class OnlineUsersChecker implements XmppConnectedCallable<List<User>> {

		@Nonnull
		private final Account account;

		@Nonnull
		private final List<User> users;

		public OnlineUsersChecker(@Nonnull Account account, @Nonnull List<User> users) {
			this.account = account;
			this.users = users;
		}

		@Override
		public List<User> call(@Nonnull Connection connection) throws AccountConnectionException, XMPPException {
			final List<User> result = new ArrayList<User>();

			final Roster roster = connection.getRoster();
			final Collection<RosterEntry> entries = roster.getEntries();
			for (final User user : users) {

				final boolean online;
				if (account.getUser().equals(user)) {
					// realm user => always online
					online = true;
				} else {
					final RosterEntry entry = Iterables.find(entries, new Predicate<RosterEntry>() {
						@Override
						public boolean apply(@Nullable RosterEntry entry) {
							return entry != null && user.getEntity().getRealmEntityId().equals(entry.getUser());
						}
					}, null);

					if (entry != null) {
						final Presence presence = roster.getPresence(entry.getUser());
						online = presence.isAvailable();
					} else {
						online = false;
					}
				}

				result.add(user.cloneWithNewStatus(online));
			}

			return result;
		}
	}
}
