package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEventType;
import org.solovyev.android.messenger.users.UserService;

import javax.annotation.Nonnull;
import java.util.Collection;

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
        Log.d(TAG, "Roster entries added: " + contactIds);
    }

    @Override
    public void entriesUpdated(@Nonnull Collection<String> contactIds) {
        Log.d(TAG, "Roster entries updated: " + contactIds);
    }

    @Override
    public void entriesDeleted(@Nonnull Collection<String> contactIds) {
        Log.d(TAG, "Roster entries deleted: " + contactIds);
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
