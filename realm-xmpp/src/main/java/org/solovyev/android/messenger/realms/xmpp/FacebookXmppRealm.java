package org.solovyev.android.messenger.realms.xmpp;

import com.google.inject.Singleton;

@Singleton
public class FacebookXmppRealm extends XmppRealm {

	public FacebookXmppRealm() {
		super("xmpp-facebook", R.string.mpp_xmpp_name_facebook, R.drawable.mpp_xmpp_facebook_icon, FacebookXmppAccountConfigurationFragment.class);
	}
}
