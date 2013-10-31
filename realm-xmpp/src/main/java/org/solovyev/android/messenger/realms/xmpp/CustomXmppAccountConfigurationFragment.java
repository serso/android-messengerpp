package org.solovyev.android.messenger.realms.xmpp;

import android.os.Bundle;
import android.view.View;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.realms.Realm;

import com.google.inject.Inject;

public class CustomXmppAccountConfigurationFragment extends XmppAccountConfigurationFragment {

	@Inject
	@Nonnull
	private CustomXmppRealm realm;

	@Nonnull
	@Override
	public Realm getRealm() {
		return realm;
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);
		root.findViewById(R.id.mpp_xmpp_resource_edittext).setVisibility(View.VISIBLE);
		root.findViewById(R.id.mpp_xmpp_resource_label).setVisibility(View.VISIBLE);
	}
}
