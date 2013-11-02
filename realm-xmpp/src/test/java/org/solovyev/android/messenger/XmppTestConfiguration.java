package org.solovyev.android.messenger;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.xmpp.CustomXmppRealm;
import org.solovyev.android.messenger.realms.xmpp.XmppRealm;


@Singleton
public class XmppTestConfiguration extends TestConfiguration {

	@Inject
	@Nonnull
	private CustomXmppRealm realm;

	@Nonnull
	@Override
	public Collection<Realm> getRealms() {
		final Collection<Realm> realms = super.getRealms();
		realms.add(realm);
		return realms;
	}
}
