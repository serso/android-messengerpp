package org.solovyev.android.messenger.realms.xmpp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.realms.Realm;

import com.google.inject.Inject;

public class GoogleXmppAccountConfigurationFragment extends XmppAccountConfigurationFragment {

	@Inject
	@Nonnull
	private GoogleXmppRealm realm;

	@Nonnull
	@Override
	public Realm getRealm() {
		return realm;
	}

	@Nullable
	@Override
	protected String getServer() {
		return "talk.google.com";
	}

	@Override
	protected int getLoginHintResId() {
		return R.string.mpp_xmpp_login_hint_google;
	}
}
