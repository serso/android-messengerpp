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

package org.solovyev.android.messenger.realms.vk;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.google.inject.Inject;
import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.showToast;

public class VkAccountConfigurationFragment extends BaseAccountConfigurationFragment<VkAccount> {

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private VkRealm realm;

    /*
	**********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */


	@Nonnull
	private EditText loginEditText;

	@Nonnull
	private EditText passwordEditText;

	public VkAccountConfigurationFragment() {
		super(R.layout.mpp_realm_conf_vk);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		loginEditText = (EditText) root.findViewById(R.id.mpp_vk_login_edittext);
		passwordEditText = (EditText) root.findViewById(R.id.mpp_vk_password_edittext);

		if (!isNewAccount()) {
			final VkAccount realm = getEditedAccount();
			final VkAccountConfiguration configuration = realm.getConfiguration();

			loginEditText.setText(configuration.getLogin());
			passwordEditText.setText(configuration.getPassword());
		}
	}

	@Override
	public AccountConfiguration validateData() {
		boolean ok = true;

		final String login = loginEditText.getText().toString();
		if (Strings.isEmpty(login)) {
			showToast(R.string.mpp_vk_login_must_be_set);
			ok = false;
		}

		final String password = passwordEditText.getText().toString();
		if (Strings.isEmpty(password)) {
			showToast(R.string.mpp_vk_password_must_be_set);
			ok = false;
		}

		if (ok) {
			return new VkAccountConfiguration(login, password);
		} else {
			return null;
		}
	}

	@Nonnull
	@Override
	public Realm getRealm() {
		return realm;
	}
}
