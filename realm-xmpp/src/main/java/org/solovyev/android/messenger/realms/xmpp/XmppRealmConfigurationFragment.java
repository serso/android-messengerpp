package org.solovyev.android.messenger.realms.xmpp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.inject.Inject;
import org.solovyev.android.messenger.realms.BaseRealmConfigurationFragment;
import org.solovyev.android.messenger.realms.RealmBuilder;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class XmppRealmConfigurationFragment extends BaseRealmConfigurationFragment<XmppRealm> {

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
    private XmppRealmDef realmDef;

    @Inject
    @Nonnull
    private RealmService realmService;

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

    @Nonnull
    private Button backButton;

    @Nonnull
    private Button saveButton;

    @Nonnull
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
        if (isNewRealm() && getMultiPaneManager().isDualPane(getActivity())) {
            // in multi pane layout we don't want to show 'Back' button as there is no 'Back' (in one pane we reuse pane for showing more than one fragment and back means to return to the previous fragment)
            backButton.setVisibility(View.GONE);
        } else {
            backButton.setVisibility(View.VISIBLE);
        }


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

    @Override
    public void onSaveInstanceState(@Nonnull Bundle out) {
        super.onSaveInstanceState(out);
    }

    @Nonnull
    @Override
    public RealmDef getRealmDef() {
        return realmDef;
    }
}
