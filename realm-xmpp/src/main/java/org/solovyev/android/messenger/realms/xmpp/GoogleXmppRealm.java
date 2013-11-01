package org.solovyev.android.messenger.realms.xmpp;

import com.google.inject.Singleton;

@Singleton
public class GoogleXmppRealm extends XmppRealm {

	public GoogleXmppRealm() {
		super("xmpp-google", R.string.mpp_xmpp_name_google, R.drawable.mpp_xmpp_google_icon, GoogleXmppAccountConfigurationFragment.class);
	}
}
