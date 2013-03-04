package org.solovyev.android.messenger.realms.xmpp;

import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;

/**
* User: serso
* Date: 3/4/13
* Time: 11:49 PM
*/
class XmppRosterListener implements RosterListener {
    @Override
    public void entriesAdded(Collection<String> addresses) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void entriesUpdated(Collection<String> addresses) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void entriesDeleted(Collection<String> addresses) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void presenceChanged(Presence presence) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
