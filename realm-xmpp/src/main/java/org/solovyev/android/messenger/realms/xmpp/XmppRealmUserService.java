package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmIsNotConnectedException;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserImpl;
import org.solovyev.android.messenger.users.UserSyncDataImpl;
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
class XmppRealmUserService implements RealmUserService {

    @Nonnull
    private final XmppRealm realm;

    XmppRealmUserService(@Nonnull XmppRealm realm) {
        this.realm = realm;
    }

    @Nullable
    @Override
    public User getUserById(@Nonnull final String realmUserId) {
        return doOnConnection(new UserLoader(realmUserId, realm));
    }

    private <R> R doOnConnection(@Nonnull ConnectedCallable<R> callable) {
        final XmppRealmConfiguration configuration = realm.getConfiguration();

        final XMPPConnection connection = new XMPPConnection(configuration.toXmppConfiguration());
        try {
            connection.connect();
            connection.login(configuration.getLogin(), configuration.getPassword(), configuration.getResource());

            return callable.call(connection);
        } catch (XMPPException e) {
            throw new RealmIsNotConnectedException(e);
        } finally {
            connection.disconnect();
        }
    }

    @Nonnull
    @Override
    public List<User> getUserContacts(@Nonnull final String realmUserId) {
        return doOnConnection(new ConnectedCallable<List<User>>() {
            @Override
            public List<User> call(@Nonnull XMPPConnection connection) throws RealmIsNotConnectedException, XMPPException {

                final Collection<RosterEntry> entries = connection.getRoster().getEntries();
                return Lists.newArrayList(Collections2.transform(entries, new Function<RosterEntry, User>() {
                    @Override
                    public User apply(@Nullable RosterEntry entry) {
                        return entry != null ? toUser(entry, realm, false) : null;
                    }
                }));
            }
        });
    }

    @Nonnull
    @Override
    public List<User> checkOnlineUsers(@Nonnull final List<User> users) {
        return doOnConnection(new ConnectedCallable<List<User>>() {
            @Override
            public List<User> call(@Nonnull XMPPConnection connection) throws RealmIsNotConnectedException, XMPPException {
                final List<User> result = new ArrayList<User>();

                final Roster roster = connection.getRoster();
                final Collection<RosterEntry> entries = roster.getEntries();
                for (final RosterEntry entry : entries) {
                    final User user = Iterables.find(users, new Predicate<User>() {
                        @Override
                        public boolean apply(@Nullable User input) {
                            return input != null && input.getRealmUser().getRealmId().equals(entry.getName());
                        }
                    }, null);

                    if (user != null) {
                        final Presence presence = roster.getPresence(entry.getUser());
                        result.add(toUser(entry, realm, presence.isAvailable()));
                    }
                }

                return result;
            }
        });
    }

    @Nonnull
    @Override
    public List<AProperty> getUserProperties(@Nonnull User user, @Nonnull Context context) {
        // todo serso: user properties
        return Collections.emptyList();
    }

    private static interface ConnectedCallable<R> {

        R call(@Nonnull XMPPConnection connection) throws RealmIsNotConnectedException, XMPPException;

    }

    private static class UserLoader implements ConnectedCallable<User> {

        @Nonnull
        private final String realmUserId;

        @Nonnull
        private final Realm realm;

        public UserLoader(@Nonnull String realmUserId, @Nonnull Realm realm) {
            this.realmUserId = realmUserId;
            this.realm = realm;
        }

        @Override
        public User call(@Nonnull XMPPConnection connection) throws RealmIsNotConnectedException, XMPPException {
            final User result;

            final RosterEntry xmppUser = connection.getRoster().getEntry(realmUserId);
            if ( xmppUser != null ) {
                result = toUser(xmppUser, realm, false);
            } else {
                result = null;
            }

            return result;
        }
    }

    private static User toUser(@Nonnull RosterEntry entry, @Nonnull Realm realm, boolean available) {
        User result;// todo serso: load properties
               /* final VCard vCard = new VCard();
                vCard.load(connection, realmUserId);*/
        final List<AProperty> properties = new ArrayList<AProperty>();
        properties.add(APropertyImpl.newInstance(User.PROPERTY_ONLINE, String.valueOf(available)));
        result = UserImpl.newInstance(realm.newRealmEntity(entry.getUser()), UserSyncDataImpl.newNeverSyncedInstance(), properties);
        return result;
    }
}
