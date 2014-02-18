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

package org.solovyev.android.messenger.realms.whatsapp;

import android.os.Bundle;
import android.view.View;
import com.google.inject.Inject;
import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment;
import org.solovyev.android.messenger.realms.Realm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class WhatsAppAccountConfigurationFragment extends BaseAccountConfigurationFragment<WhatsAppAccount> {

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private WhatsAppRealm realm;

	public WhatsAppAccountConfigurationFragment() {
		super(R.layout.mpp_realm_whatsapp_conf);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		if (!isNewAccount()) {
			final WhatsAppAccount account = getEditedAccount();
			final WhatsAppAccountConfiguration configuration = account.getConfiguration();


		}
	}

	@Nullable
	@Override
	public AccountConfiguration validateData() {
		return new WhatsAppAccountConfiguration();
	}

	@Nonnull
	@Override
	public Realm getRealm() {
		return realm;
	}
}
