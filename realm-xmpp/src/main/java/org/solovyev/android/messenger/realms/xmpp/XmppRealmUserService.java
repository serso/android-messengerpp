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
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmEntityImpl;
import org.solovyev.android.messenger.realms.RealmIsNotConnectedException;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserImpl;
import org.solovyev.android.messenger.users.UserSyncDataImpl;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.APropertyImpl;
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
class XmppRealmUserService extends AbstractXmppRealmService implements RealmUserService {

    @Nonnull
    private static final String TAG = "M++/" + XmppRealmUserService.class.getSimpleName();

    XmppRealmUserService(@Nonnull XmppRealm realm, @Nonnull XmppConnectionAware connectionAware) {
        super(realm, connectionAware);
    }

    @Nullable
    @Override
    public User getUserById(@Nonnull final String realmUserId) {
        return doOnConnection(new UserLoader(getRealm(), realmUserId));
    }

    @Nonnull
    @Override
    public List<User> getUserContacts(@Nonnull final String realmUserId) {
        return doOnConnection(new UserContactsLoader(getRealm(), realmUserId));
    }

    @Nonnull
    @Override
    public List<User> checkOnlineUsers(@Nonnull final List<User> users) {
        return doOnConnection(new OnlineUsersChecker(getRealm(), users));
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
        private final Realm realm;

        @Nonnull
        private final String realmUserId;

        public UserLoader(@Nonnull Realm realm, @Nonnull String realmUserId) {
            this.realm = realm;
            this.realmUserId = realmUserId;
        }

        @Override
        public User call(@Nonnull Connection connection) throws RealmIsNotConnectedException, XMPPException {
            final User result;

            if (realm.getUser().getRealmUser().getRealmEntityId().equals(realmUserId)) {
                // realm user cannot be found in roster ->  information should be loaded separately
                result = toUser(realm.getId(), realmUserId, null, true, connection);
            } else {
                // try to find user contacts in roster
                final RosterEntry entry = connection.getRoster().getEntry(realmUserId);
                if (entry != null) {
                    result = toUser(realm.getId(), entry.getUser(), entry.getName(), false, connection);
                } else {
                    result = null;
                }
            }

            return result;
        }
    }

    @Nonnull
    public static User toUser(@Nonnull String realmId, @Nonnull String realmUserId, @Nullable String name, boolean available, @Nonnull Connection connection) throws XMPPException {
        // todo serso: problem loading VCards - timeout exception
        final List<AProperty> properties = loadUserProperties(true, realmUserId, available, connection, name);
        return UserImpl.newInstance(RealmEntityImpl.newInstance(realmId, realmUserId), UserSyncDataImpl.newNeverSyncedInstance(), properties);
    }

    @Nonnull
    private static List<AProperty> loadUserProperties(boolean loadVCard,
                                                      @Nonnull String realmUserId,
                                                      boolean available,
                                                      @Nonnull Connection connection,
                                                      @Nullable String name) throws XMPPException {
        final List<AProperty> result = new ArrayList<AProperty>();

        result.add(APropertyImpl.newInstance(User.PROPERTY_ONLINE, String.valueOf(available)));

        if (loadVCard) {
            try {

                final VCard userCard = new VCard();

                userCard.load(connection, realmUserId);

                result.add(APropertyImpl.newInstance(User.PROPERTY_FIRST_NAME, userCard.getFirstName()));
                result.add(APropertyImpl.newInstance(User.PROPERTY_LAST_NAME, userCard.getLastName()));
                result.add(APropertyImpl.newInstance(User.PROPERTY_NICKNAME, userCard.getNickName()));
                result.add(APropertyImpl.newInstance(User.PROPERTY_EMAIL, userCard.getEmailHome()));
                result.add(APropertyImpl.newInstance(User.PROPERTY_PHONE, userCard.getPhoneHome("VOICE")));
                result.add(APropertyImpl.newInstance(XmppRealmDef.USER_PROPERTY_AVATAR_HASH, userCard.getAvatarHash()));

                final byte[] avatar = userCard.getAvatar();
                if (avatar != null) {
                    result.add(APropertyImpl.newInstance(XmppRealmDef.USER_PROPERTY_AVATAR_BASE64, ABase64StringEncoder.getInstance().convert(avatar)));
                }

                // full name
                final String fullName = userCard.getField("FN");
                tryParseNameProperties(result, fullName);
            } catch (XMPPException e) {
                // For some reason vcard loading may return timeout exception => investigate this behaviour
                // NOTE: pidgin loads user information also very slow
                Log.w(TAG, e.getMessage(), e);
            }
        } else {
            tryParseNameProperties(result, name);
        }

        return result;
    }

    private static void tryParseNameProperties(@Nonnull List<AProperty> result, @Nullable String fullName) {
        if (fullName != null) {
            int firstSpaceSymbolIndex = fullName.indexOf(' ');
            int lastSpaceSymbolIndex = fullName.lastIndexOf(' ');
            if (firstSpaceSymbolIndex != -1 && firstSpaceSymbolIndex == lastSpaceSymbolIndex) {
                // only one space in the string
                // Proof:
                // 1. if no spaces => both return -1
                // 2. if more than one spaces => both return different
                final String firstName = fullName.substring(0, firstSpaceSymbolIndex);
                final String lastName = fullName.substring(firstSpaceSymbolIndex + 1);
                result.add(APropertyImpl.newInstance(User.PROPERTY_FIRST_NAME, firstName));
                result.add(APropertyImpl.newInstance(User.PROPERTY_LAST_NAME, lastName));
            } else {
                // just store full name in first name field
                result.add(APropertyImpl.newInstance(User.PROPERTY_FIRST_NAME, fullName));
            }
        }
    }

    private static class UserContactsLoader implements XmppConnectedCallable<List<User>> {

        @Nonnull
        private final Realm realm;

        @Nonnull
        private final String realmUserId;

        private UserContactsLoader(@Nonnull Realm realm, @Nonnull String realmUserId) {
            this.realm = realm;
            this.realmUserId = realmUserId;
        }

        @Override
        public List<User> call(@Nonnull final Connection connection) throws RealmIsNotConnectedException, XMPPException {

            if (realm.getUser().getRealmUser().getRealmEntityId().equals(realmUserId)) {
                // realm user => load contacts through the roster
                final Collection<RosterEntry> entries = connection.getRoster().getEntries();

                final List<User> result = new ArrayList<User>(entries.size());
                for (RosterEntry entry : entries) {
                    result.add(toUser(realm.getId(), entry.getUser(), entry.getName(), false, connection));
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
        private final Realm realm;

        @Nonnull
        private final List<User> users;

        public OnlineUsersChecker(@Nonnull Realm realm, @Nonnull List<User> users) {
            this.realm = realm;
            this.users = users;
        }

        @Override
        public List<User> call(@Nonnull Connection connection) throws RealmIsNotConnectedException, XMPPException {
            final List<User> result = new ArrayList<User>();

            final Roster roster = connection.getRoster();
            final Collection<RosterEntry> entries = roster.getEntries();
            for (final User user : users) {

                final boolean online;
                if (realm.getUser().equals(user)) {
                    // realm user => always online
                    online = true;
                } else {
                    final RosterEntry entry = Iterables.find(entries, new Predicate<RosterEntry>() {
                        @Override
                        public boolean apply(@Nullable RosterEntry entry) {
                            return entry != null && user.getRealmUser().getRealmEntityId().equals(entry.getUser());
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
