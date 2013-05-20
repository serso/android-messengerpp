package org.solovyev.android.messenger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.realms.vk.VkRealmDef;
import org.solovyev.android.messenger.realms.xmpp.XmppRealmDef;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:36 PM
 */
@Singleton
public class MessengerConfigurationImpl implements MessengerConfiguration {

	@Nonnull
	private final List<RealmDef> realmDefs = new ArrayList<RealmDef>();

	@Inject
	@Nonnull
	private XmppRealmDef xmppRealmDef;

	@Inject
	@Nonnull
	private VkRealmDef mVkRealmDef;

	public MessengerConfigurationImpl() {
	}

	@Nonnull
	@Override
	public Collection<RealmDef> getRealmDefs() {
		synchronized (realmDefs) {
			if (realmDefs.isEmpty()) {
				realmDefs.add(xmppRealmDef);
				realmDefs.add(mVkRealmDef);
			}
		}

		return this.realmDefs;
	}
}
