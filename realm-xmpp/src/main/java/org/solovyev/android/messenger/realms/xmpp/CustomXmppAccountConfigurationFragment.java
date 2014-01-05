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
import android.widget.TextView;
import com.google.inject.Inject;
import org.solovyev.android.messenger.realms.Realm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.text.Html.fromHtml;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.solovyev.android.messenger.App.showToast;
import static org.solovyev.android.messenger.realms.xmpp.XmppAccountConfiguration.DEFAULT_PORT;

public class CustomXmppAccountConfigurationFragment extends XmppAccountConfigurationFragment {

	@Nonnull
	private static final String ADVANCED_OPTIONS_SHOWN = "advanced_options_shown";

	/*
	**********************************************************************
	*
	*                           FIELDS
	*
	**********************************************************************
	*/

	@Inject
	@Nonnull
	private CustomXmppRealm realm;

	/*
	**********************************************************************
	*
	*                           VIEWS
	*
	**********************************************************************
	*/

	@Nonnull
	private EditText resourceEditText;

	@Nonnull
	private TextView advancedOptionsLinkTextView;

	@Nonnull
	private View advancedOptionsView;

	@Nonnull
	private EditText portEditText;

	public CustomXmppAccountConfigurationFragment() {
		super(R.layout.mpp_realm_conf_xmpp_custom);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		resourceEditText = (EditText) root.findViewById(R.id.mpp_xmpp_resource_edittext);
		portEditText = (EditText) root.findViewById(R.id.mpp_xmpp_port_edittext);
		advancedOptionsView = root.findViewById(R.id.mpp_xmpp_advanced_options);

		if (!isNewAccount()) {
			final XmppAccount realm = getEditedAccount();
			final XmppAccountConfiguration configuration = realm.getConfiguration();

			portEditText.setText(configuration.getPort());
			resourceEditText.setText(configuration.getResource());
		} else {
			portEditText.setText(String.valueOf(DEFAULT_PORT));
		}

		advancedOptionsLinkTextView = (TextView) root.findViewById(R.id.mpp_xmpp_advanced_options_link);
		advancedOptionsLinkTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleAdvancedOptions(!advancedOptionsView.isShown());
			}
		});

		if (savedInstanceState != null) {
			toggleAdvancedOptions(savedInstanceState.getBoolean(ADVANCED_OPTIONS_SHOWN, false));
		} else {
			hideAdvancedOptions();
		}
	}

	private void toggleAdvancedOptions(boolean show) {
		if (show) {
			showAdvancedOptions();
		} else {
			hideAdvancedOptions();
		}
	}

	private void showAdvancedOptions() {
		advancedOptionsLinkTextView.setText(fromHtml(getString(R.string.mpp_xmpp_hide_advanced_options)));
		advancedOptionsView.setVisibility(VISIBLE);
	}

	private void hideAdvancedOptions() {
		advancedOptionsLinkTextView.setText(fromHtml(getString(R.string.mpp_xmpp_show_advanced_options)));
		advancedOptionsView.setVisibility(GONE);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (wasViewCreated()) {
			outState.putBoolean(ADVANCED_OPTIONS_SHOWN, advancedOptionsView.isShown());
		}
	}

	@Nullable
	@Override
	protected XmppAccountConfiguration validateData(@Nullable String server, @Nullable String login, @Nullable String password) {
		XmppAccountConfiguration configuration = super.validateData(server, login, password);
		if (configuration != null) {
			final String resource = resourceEditText.getText().toString();
			if (resource != null) {
				configuration.setResource(resource);
			}

			final String port = portEditText.getText().toString();
			if (port != null) {
				try {
					configuration.setPort(Integer.valueOf(port));
				} catch (NumberFormatException e) {
					showToast(R.string.mpp_xmpp_invalid_port_number);
					configuration = null;
				}
			}
		}

		return configuration;
	}

	@Nonnull
	@Override
	public Realm getRealm() {
		return realm;
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
