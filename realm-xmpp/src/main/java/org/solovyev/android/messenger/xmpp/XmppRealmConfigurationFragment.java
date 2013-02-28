package org.solovyev.android.messenger.xmpp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.realms.BaseRealmConfigurationFragment;
import org.solovyev.android.messenger.realms.RealmBuilder;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.common.text.Strings;

public class XmppRealmConfigurationFragment extends BaseRealmConfigurationFragment<XmppRealm> {

    @Inject
    @NotNull
    private XmppRealmDef realmDef;

    @Inject
    @NotNull
    private RealmService realmService;

    @NotNull
    private EditText serverEditText;

    @NotNull
    private EditText loginEditText;

    @NotNull
    private EditText passwordEditText;

    @NotNull
    private EditText resourceEditText;

    @NotNull
    private Button backButton;

    @NotNull
    private Button saveButton;

    @NotNull
    private Button removeButton;

    public XmppRealmConfigurationFragment() {
        super(R.layout.mpp_realm_conf_xmpp);
    }

    @Override
    public void onViewCreated(View root, Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        serverEditText = (EditText) root.findViewById(R.id.mpp_xmpp_server_edittext);
        loginEditText = (EditText) root.findViewById(R.id.mpp_xmpp_login_edittext);
        passwordEditText = (EditText) root.findViewById(R.id.mpp_xmpp_password_edittext);
        resourceEditText = (EditText) root.findViewById(R.id.mpp_xmpp_resource_edittext);

        if (!isNewRealm()) {
            final XmppRealm realm = getEditedRealm();
            final XmppRealmConfiguration configuration = realm.getConfiguration();

            serverEditText.setText(configuration.getServer());
            loginEditText.setText(configuration.getLogin());
            passwordEditText.setText(configuration.getPassword());
            resourceEditText.setText(configuration.getResource());
        }

        removeButton = (Button) root.findViewById(R.id.mpp_xmpp_remove_button);
        if (isNewRealm()) {
            removeButton.setVisibility(View.GONE);
        } else {
            removeButton.setVisibility(View.VISIBLE);
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeRealm(getEditedRealm());
                }
            });
        }

        backButton = (Button) root.findViewById(R.id.mpp_xmpp_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backButtonPressed();
            }
        });

        saveButton = (Button) root.findViewById(R.id.mpp_xmpp_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRealm();
            }
        });
    }

    private void saveRealm() {
        final String server = serverEditText.getText().toString();
        final String login = loginEditText.getText().toString();
        final String password = passwordEditText.getText().toString();
        final String resource = resourceEditText.getText().toString();

        final XmppRealmConfiguration configuration = validateData(server, login, password, resource);
        if (configuration != null) {
            final RealmBuilder realmBuilder = realmDef.newRealmBuilder(configuration, getEditedRealm());
            saveRealm(realmBuilder, null);
        }
    }

    @Nullable
    private XmppRealmConfiguration validateData(@Nullable String server, @Nullable String login, @Nullable String password, @Nullable String resource) {
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
            final XmppRealmConfiguration result = new XmppRealmConfiguration(server, login, password);
            if (resource != null) {
                result.setResource(resource);
            }
            return result;
        } else {
            return null;
        }
    }
}
