/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.realms.xmpp;

import android.os.Bundle;
import android.view.View;
import com.google.inject.Inject;
import org.solovyev.android.messenger.realms.Realm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

	@Nullable
	@Override
	protected String getServer() {
		return null;
	}

	protected int getLoginHintResId() {
		return R.string.mpp_xmpp_login_hint_custom;
	}

	@Nullable
	@Override
	protected String getDefaultDomain() {
		return null;
	}

}
