package org.solovyev.android.messenger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.realms.xmpp.XmppRealmDef;

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
	private XmppRealmDef xmppRealmDef;

	@Nonnull
	@Override
	public Collection<RealmDef> getRealmDefs() {
		return Arrays.<RealmDef>asList(xmppRealmDef);
	}
}
