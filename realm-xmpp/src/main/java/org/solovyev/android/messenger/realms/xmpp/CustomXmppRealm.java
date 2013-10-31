package org.solovyev.android.messenger.realms.xmpp;

import com.google.inject.Singleton;

@Singleton
public class CustomXmppRealm extends XmppRealm {

	public CustomXmppRealm() {
		super("xmpp", R.string.mpp_xmpp_name, R.drawable.mpp_xmpp_icon, CustomXmppAccountConfigurationFragment.class);
	}
}
