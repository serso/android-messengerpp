package org.solovyev.android.messenger.realms.xmpp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.realms.Realm;

import com.google.inject.Inject;

public class FacebookXmppAccountConfigurationFragment extends XmppAccountConfigurationFragment {

	@Inject
	@Nonnull
	private FacebookXmppRealm realm;

	@Nonnull
	@Override
	public Realm getRealm() {
		return realm;
	}

	@Nullable
	@Override
	protected String getServer() {
		return "chat.facebook.com";
	}

	@Override
	protected int getLoginHintResId() {
		return R.string.mpp_xmpp_facebook_login_hint;
	}
}
