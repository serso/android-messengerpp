package org.solovyev.android.messenger.realms.xmpp;

import org.jivesoftware.smack.packet.Presence;
import org.junit.Test;
import org.solovyev.android.messenger.XmppTest;

public class XmppRosterListenerTest extends XmppTest {

	@Test
	public void testShouldCallUserServiceOnPresenceChange() throws Exception {
		final XmppRosterListener l = new XmppRosterListener(getXmppAccount());
		final Presence presence = new Presence(Presence.Type.available, "test", 0, Presence.Mode.available);
		presence.setFrom("test");
		l.presenceChanged(presence);
		// todo serso: ...
	}
}
