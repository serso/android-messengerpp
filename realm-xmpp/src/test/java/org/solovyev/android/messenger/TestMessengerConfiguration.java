package org.solovyev.android.messenger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.xmpp.XmppRealm;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

/**
 * User: serso
 * Date: 2/27/13
 * Time: 9:42 PM
 */
@Singleton
public class TestMessengerConfiguration implements MessengerConfiguration {

	@Inject
	@Nonnull
	private XmppRealm xmppRealmDef;

	@Nonnull
	@Override
	public Collection<Realm> getRealms() {
		return Arrays.<Realm>asList(xmppRealmDef);
	}
}
