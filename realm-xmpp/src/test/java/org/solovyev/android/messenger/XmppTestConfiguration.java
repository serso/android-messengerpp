package org.solovyev.android.messenger;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.xmpp.XmppRealm;


/**
 * User: serso
 * Date: 2/27/13
 * Time: 9:42 PM
 */
public class XmppTestConfiguration extends TestConfiguration {

	@Nonnull
	@Override
	public Collection<Realm> getRealms() {
		final Collection<Realm> realms = super.getRealms();
		realms.add(new XmppRealm());
		return realms;
	}
}
