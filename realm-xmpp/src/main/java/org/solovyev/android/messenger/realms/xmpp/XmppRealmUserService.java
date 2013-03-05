package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmIsNotConnectedException;
import org.solovyev.android.messenger.users.*;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.APropertyImpl;

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

    XmppRealmUserService(@Nonnull XmppRealm realm) {
        super(realm);
    }

    @Nullable
    @Override
    public User getUserById(@Nonnull final String realmUserId) {
        return doConnected(new UserLoader(getRealm(), realmUserId));
    }

    @Nonnull
    @Override
    public List<User> getUserContacts(@Nonnull final String realmUserId) {
        return doConnected(new UserContactsLoader(getRealm(), realmUserId));
    }

    @Nonnull
    @Override
    public List<User> checkOnlineUsers(@Nonnull final List<User> users) {
        return doConnected(new OnlineUsersChecker(getRealm(), users));
    }

    @Nonnull
    @Override
    public List<AProperty> getUserProperties(@Nonnull User user, @Nonnull Context context) {
        final List<AProperty> result = new ArrayList<AProperty>(user.getProperties().size());

        for (AProperty property : user.getProperties()) {
            final String name = property.getName();
            if ( name.equals(User.PROPERTY_NICKNAME) ) {
                result.add(APropertyImpl.newInstance(context.getString(R.string.mpp_nickname), property.getValue()));
            }
        }

        return result;
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
        public User call(@Nonnull XMPPConnection connection) throws RealmIsNotConnectedException, XMPPException {
            final User result;

            if (realm.getUser().getRealmUser().getRealmEntityId().equals(realmUserId)) {
                // realm user cannot be found in roster ->  information should be loaded separately
                result = toUser(realmUserId, realm, true, connection);
            } else {
                // try to find user contacts in roster
                final RosterEntry xmppUser = connection.getRoster().getEntry(realmUserId);
                if (xmppUser != null) {
                    result = toUser(xmppUser.getUser(), realm, false, connection);
                } else {
                    result = null;
                }
            }

            return result;
        }
    }

    private static User toUser(@Nonnull String realmUserId, @Nonnull Realm realm, boolean available, @Nonnull Connection connection) throws XMPPException {

        final VCard userCard = new VCard();
        userCard.load(connection, realmUserId);

        final List<AProperty> properties = new ArrayList<AProperty>();
        properties.add(APropertyImpl.newInstance(User.PROPERTY_ONLINE, String.valueOf(available)));
        properties.add(APropertyImpl.newInstance(User.PROPERTY_FIRST_NAME, userCard.getFirstName()));
        properties.add(APropertyImpl.newInstance(User.PROPERTY_LAST_NAME, userCard.getLastName()));
        properties.add(APropertyImpl.newInstance(User.PROPERTY_NICKNAME, userCard.getNickName()));
        properties.add(APropertyImpl.newInstance(User.PROPERTY_EMAIL, userCard.getEmailHome()));
        properties.add(APropertyImpl.newInstance(User.PROPERTY_PHONE, userCard.getPhoneHome("VOICE")));

        // full name
        final String fullName = userCard.getField("FN");
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
                properties.add(APropertyImpl.newInstance(User.PROPERTY_FIRST_NAME, firstName));
                properties.add(APropertyImpl.newInstance(User.PROPERTY_LAST_NAME, lastName));
            } else {
                // just store full name in first name field
                properties.add(APropertyImpl.newInstance(User.PROPERTY_FIRST_NAME, fullName));
            }
        }


        return UserImpl.newInstance(realm.newRealmEntity(realmUserId), UserSyncDataImpl.newNeverSyncedInstance(), properties);
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
        public List<User> call(@Nonnull final XMPPConnection connection) throws RealmIsNotConnectedException, XMPPException {

            if (realm.getUser().getRealmUser().getRealmEntityId().equals(realmUserId)) {
                // realm user => load contacts through the roster
                final Collection<RosterEntry> entries = connection.getRoster().getEntries();

                final List<User> result = new ArrayList<User>(entries.size());
                for (RosterEntry entry : entries) {
                    result.add(toUser(entry.getUser(), realm, false, connection));
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
        public List<User> call(@Nonnull XMPPConnection connection) throws RealmIsNotConnectedException, XMPPException {
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
