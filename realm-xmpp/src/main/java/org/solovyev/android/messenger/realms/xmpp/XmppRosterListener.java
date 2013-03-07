package org.solovyev.android.messenger.realms.xmpp;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEventType;
import org.solovyev.android.messenger.users.UserImpl;
import org.solovyev.android.messenger.users.UserService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
* User: serso
* Date: 3/4/13
* Time: 11:49 PM
*/
class XmppRosterListener implements RosterListener {

    @Nonnull
    private static final String TAG = "M++/XmppRosterListener";

    @Nonnull
    private final XmppRealm realm;

    @Nonnull
    private final XmppConnectionAware connectionAware;

    XmppRosterListener(@Nonnull XmppRealm realm, @Nonnull XmppConnectionAware connectionAware) {
        this.realm = realm;
        this.connectionAware = connectionAware;
    }

    @Override
    public void entriesAdded(@Nonnull Collection<String> contactIds) {
        processEntries(contactIds);
    }

    private void processEntries(@Nonnull Collection<String> contactIds) {
        final List<User> contacts = Lists.newArrayList(Iterables.transform(contactIds, new Function<String, User>() {
            @Override
            public User apply(@Nullable String contactId) {
                assert contactId != null;
                return UserImpl.newFakeInstance(realm.newRealmEntity(contactId));
            }
        }));
        // we cannot allow delete because we don't know if user is really deleted on remote server - we only know that his presence was changed
        // we cannot allow update because we don't have fully loaded user
        getUserService().mergeUserContacts(realm.getUser().getRealmEntity(), contacts, false, false);
    }

    @Override
    public void entriesUpdated(@Nonnull Collection<String> contactIds) {
        processEntries(contactIds);
    }

    @Override
    public void entriesDeleted(@Nonnull Collection<String> contactIds) {
        processEntries(contactIds);
    }

    @Override
    public void presenceChanged(@Nonnull final Presence presence) {
        final String realmUserId = presence.getFrom();

        final User contact = getUserService().getUserById(realm.newRealmEntity(realmUserId));
        final UserEventType userEventType = presence.isAvailable() ? UserEventType.contact_online : UserEventType.contact_offline;
        getUserService().fireUserEvent(realm.getUser(), userEventType, contact);
    }

    @Nonnull
    private UserService getUserService() {
        return MessengerApplication.getServiceLocator().getUserService();
    }
}
