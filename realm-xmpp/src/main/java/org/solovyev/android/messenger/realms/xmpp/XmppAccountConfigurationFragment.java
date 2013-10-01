package org.solovyev.android.messenger.realms.xmpp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.inject.Inject;
import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class XmppAccountConfigurationFragment extends BaseAccountConfigurationFragment<XmppAccount> {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	// todo serso: save instance state

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private XmppRealm realmDef;

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

			serverEditText.setText(configuration.getServer());
			loginEditText.setText(configuration.getLogin());
			passwordEditText.setText(configuration.getPassword());
			resourceEditText.setText(configuration.getResource());
		}
	}

	@Override
	protected AccountConfiguration validateData() {
		final String server = serverEditText.getText().toString();
		final String login = loginEditText.getText().toString();
		final String password = passwordEditText.getText().toString();
		final String resource = resourceEditText.getText().toString();

		return validateData(server, login, password, resource);
	}

	@Nullable
	private XmppAccountConfiguration validateData(@Nullable String server, @Nullable String login, @Nullable String password, @Nullable String resource) {
		boolean ok = true;

		if (Strings.isEmpty(server)) {
			Toast.makeText(getActivity(), "Server field must be set!", Toast.LENGTH_SHORT).show();
			ok = false;
		}

		if (Strings.isEmpty(login)) {
			Toast.makeText(getActivity(), "Login field must be set!", Toast.LENGTH_SHORT).show();
			ok = false;
		}

		if (Strings.isEmpty(password)) {
			Toast.makeText(getActivity(), "Password field must be set!", Toast.LENGTH_SHORT).show();
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

	@Override
	public void onSaveInstanceState(@Nonnull Bundle out) {
		super.onSaveInstanceState(out);
	}

	@Nonnull
	@Override
	public Realm getRealm() {
		return realmDef;
	}
}
