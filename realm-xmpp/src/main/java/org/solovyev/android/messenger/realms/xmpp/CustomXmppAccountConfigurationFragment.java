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
import android.widget.*;
import com.google.inject.Inject;
import org.solovyev.android.messenger.realms.Realm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.BLUE;
import static android.graphics.Paint.UNDERLINE_TEXT_FLAG;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.solovyev.android.messenger.App.showToast;
import static org.solovyev.android.messenger.realms.xmpp.XmppAccountConfiguration.*;
import static org.solovyev.common.text.Strings.isEmpty;

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

	@Nonnull
	private Spinner securityModeSpinner;

	@Nonnull
	private CheckBox useUsernameWithoutDomainCheckbox;

	public CustomXmppAccountConfigurationFragment() {
		super(R.layout.mpp_realm_conf_xmpp_custom);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final EditText loginEditText = getLoginEditText();
		loginEditText.setOnFocusChangeListener(new LoginOnFocusChangeListener(loginEditText, getServerEditText()));

		resourceEditText = (EditText) root.findViewById(R.id.mpp_xmpp_resource_edittext);
		useUsernameWithoutDomainCheckbox = (CheckBox) root.findViewById(R.id.mpp_xmpp_use_username_without_domain_checkbox);
		portEditText = (EditText) root.findViewById(R.id.mpp_xmpp_port_edittext);
		securityModeSpinner = (Spinner) root.findViewById(R.id.mpp_xmpp_security_mode_spinner);
		securityModeSpinner.setAdapter(createAdapter());
		securityModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (parent.getChildCount() > 0) {
					final View child = parent.getChildAt(0);
					if (child instanceof TextView) {
						((TextView) child).setTextColor(portEditText.getCurrentTextColor());
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		advancedOptionsView = root.findViewById(R.id.mpp_xmpp_advanced_options);

		if (!isNewAccount()) {
			final XmppAccount realm = getEditedAccount();
			final XmppAccountConfiguration configuration = realm.getConfiguration();

			portEditText.setText(String.valueOf(configuration.getPort()));
			resourceEditText.setText(configuration.getResource());
			securityModeSpinner.setSelection(configuration.getSecurityMode().ordinal());
			useUsernameWithoutDomainCheckbox.setChecked(!configuration.isUseLoginWithDomain());
		} else {
			portEditText.setText(String.valueOf(DEFAULT_PORT));
			resourceEditText.setText(DEFAULT_RESOURCE);
			securityModeSpinner.setSelection(DEFAULT_SECURITY_MODE.ordinal());
			useUsernameWithoutDomainCheckbox.setChecked(!DEFAULT_USE_LOGIN_WITH_DOMAIN);
		}

		advancedOptionsLinkTextView = (TextView) root.findViewById(R.id.mpp_xmpp_advanced_options_link);
		advancedOptionsLinkTextView.setTextColor(BLUE);
		advancedOptionsLinkTextView.setPaintFlags(advancedOptionsLinkTextView.getPaintFlags() | UNDERLINE_TEXT_FLAG);
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


	@Nonnull
	private ArrayAdapter<String> createAdapter() {
		final List<String> items = new ArrayList<String>();
		for (XmppSecurityMode mode : XmppSecurityMode.values()) {
			items.add(getString(mode.getNameResId()));
		}
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getThemeContext(), android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}


	private void toggleAdvancedOptions(boolean show) {
		if (show) {
			showAdvancedOptions();
		} else {
			hideAdvancedOptions();
		}
	}

	private void showAdvancedOptions() {
		advancedOptionsLinkTextView.setText(getString(R.string.mpp_xmpp_hide_advanced_options));
		advancedOptionsView.setVisibility(VISIBLE);
	}

	private void hideAdvancedOptions() {
		advancedOptionsLinkTextView.setText(getString(R.string.mpp_xmpp_show_advanced_options));
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

			configuration.setSecurityMode(XmppSecurityMode.values()[securityModeSpinner.getSelectedItemPosition()]);
			configuration.setUseLoginWithDomain(!useUsernameWithoutDomainCheckbox.isChecked());

			final String domain = configuration.getDomain();
			if (isEmpty(domain)) {
				showToast(R.string.mpp_xmpp_no_domain_in_username);
				return null;
			}

			final String port = portEditText.getText().toString();
			if (port != null) {
				try {
					configuration.setPort(Integer.valueOf(port));
				} catch (NumberFormatException e) {
					showToast(R.string.mpp_xmpp_invalid_port_number);
					return null;
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

	/**
	 * This listeners tries to add domain.xx(=server) to login if it doesn't exist.
	 */
	private static class LoginOnFocusChangeListener implements View.OnFocusChangeListener {

		@Nonnull
		private final EditText loginEditText;

		@Nonnull
		private final EditText serverEditText;

		public LoginOnFocusChangeListener(@Nonnull EditText loginEditText, @Nonnull EditText serverEditText) {
			this.loginEditText = loginEditText;
			this.serverEditText = serverEditText;
		}

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				final String login = loginEditText.getText().toString();
				if (!isEmpty(login)) {
					final String server = serverEditText.getText().toString();
					if (!isEmpty(server)) {
						// login and server fields are set => let's check if domain exists in login
						final String domain = XmppAccountConfiguration.getAfterAt(login);
						if (isEmpty(domain)) {
							// no domain => set it manually
							if (login.endsWith(String.valueOf(XmppAccountConfiguration.AT))) {
								loginEditText.setText(login + server);
							} else {
								loginEditText.setText(login + XmppAccountConfiguration.AT + server);
							}
						}
					}
				}
			}
		}
	}
}
