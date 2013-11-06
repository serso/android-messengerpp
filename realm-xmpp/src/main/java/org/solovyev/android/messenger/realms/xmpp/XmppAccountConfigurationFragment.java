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
import android.widget.EditText;
import com.google.inject.Inject;
import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.view.View.GONE;
import static android.view.View.NO_ID;
import static org.solovyev.android.messenger.App.showToast;
import static org.solovyev.common.text.Strings.isEmpty;

public abstract class XmppAccountConfigurationFragment extends BaseAccountConfigurationFragment<XmppAccount> {

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private AccountService accountService;

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	@Nonnull
	private EditText serverEditText;

	@Nonnull
	private EditText loginEditText;

	@Nonnull
	private EditText passwordEditText;

	@Nonnull
	private EditText resourceEditText;

	public XmppAccountConfigurationFragment() {
		super(R.layout.mpp_realm_conf_xmpp);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		serverEditText = (EditText) root.findViewById(R.id.mpp_xmpp_server_edittext);
		loginEditText = (EditText) root.findViewById(R.id.mpp_xmpp_login_edittext);
		passwordEditText = (EditText) root.findViewById(R.id.mpp_xmpp_password_edittext);
		resourceEditText = (EditText) root.findViewById(R.id.mpp_xmpp_resource_edittext);

		if (!isNewAccount()) {
			final XmppAccount realm = getEditedAccount();
			final XmppAccountConfiguration configuration = realm.getConfiguration();

			setupServerInput(root);
			serverEditText.setText(configuration.getServer());

			loginEditText.setText(configuration.getLogin());
			loginEditText.setEnabled(false);

			passwordEditText.setText(configuration.getPassword());
			resourceEditText.setText(configuration.getResource());
		} else {
			setupServerInput(root);
		}

		final int loginHintResId = getLoginHintResId();
		if (loginHintResId != NO_ID) {
			loginEditText.setHint(loginHintResId);
		}
	}

	private void setupServerInput(@Nonnull View root) {
		final View serverLabel = root.findViewById(R.id.mpp_xmpp_server_label);

		final String server = getServer();
		if (!isEmpty(server)) {
			serverEditText.setText(server);
			serverEditText.setVisibility(GONE);
			serverLabel.setVisibility(GONE);
		}
	}

	@Nullable
	protected String getServer() {
		return null;
	}

	protected int getLoginHintResId() {
		return R.string.mpp_xmpp_login_hint;
	}

	@Override
	public AccountConfiguration validateData() {
		final String server = serverEditText.getText().toString();
		final String login = loginEditText.getText().toString();
		final String password = passwordEditText.getText().toString();
		final String resource = resourceEditText.getText().toString();

		return validateData(server, login, password, resource);
	}

	@Nullable
	private XmppAccountConfiguration validateData(@Nullable String server, @Nullable String login, @Nullable String password, @Nullable String resource) {
		boolean ok = true;

		if (isEmpty(server)) {
			showToast(R.string.mpp_xmpp_server_must_be_set);
			ok = false;
		}

		if (isEmpty(login)) {
			showToast(R.string.mpp_xmpp_login_must_be_set);
			ok = false;
		}

		if (isEmpty(password)) {
			showToast(R.string.mpp_xmpp_password_must_be_set);
			ok = false;
		}

		if (ok) {
			final XmppAccountConfiguration result = new XmppAccountConfiguration(server, login, password);
			if (resource != null) {
				result.setResource(resource);
			}
			return result;
		} else {
			return null;
		}
	}
}
