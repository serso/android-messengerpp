package org.solovyev.android.messenger.realms.xmpp;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.users.*;

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

    XmppRosterListener(@Nonnull XmppRealm realm) {
        this.realm = realm;
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
                return Users.newEmptyUser(realm.newRealmEntity(contactId));
            }
        }));
        // we cannot allow delete because we don't know if user is really deleted on remote server - we only know that his presence was changed
        // we cannot allow update because we don't have fully loaded user
        getUserService().mergeUserContacts(realm.getUser().getEntity(), contacts, false, false);
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
        getUserService().fireEvent(userEventType.newEvent(realm.getUser(), contact));
    }

    @Nonnull
    private UserService getUserService() {
        return MessengerApplication.getServiceLocator().getUserService();
    }
}
