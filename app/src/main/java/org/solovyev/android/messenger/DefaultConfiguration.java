package org.solovyev.android.messenger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.sms.SmsRealm;
import org.solovyev.android.messenger.realms.vk.VkRealm;
import org.solovyev.android.messenger.realms.xmpp.XmppRealm;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:36 PM
 */
@Singleton
public class DefaultConfiguration implements Configuration {

	@Nonnull
	private final List<Realm> realms = new ArrayList<Realm>();

	@Inject
	@Nonnull
	private XmppRealm xmppRealm;

	@Inject
	@Nonnull
	private VkRealm vkRealm;

	@Inject
	@Nonnull
	private SmsRealm smsRealm;

	public DefaultConfiguration() {
	}

	@Nonnull
	@Override
	public Collection<Realm> getRealms() {
		synchronized (realms) {
			if (realms.isEmpty()) {
				realms.add(xmppRealm);
				realms.add(vkRealm);
				realms.add(smsRealm);
			}
		}

		return this.realms;
	}
}
