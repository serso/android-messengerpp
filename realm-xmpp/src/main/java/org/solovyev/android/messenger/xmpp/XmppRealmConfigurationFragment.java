package org.solovyev.android.messenger.xmpp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.realms.RealmAlreadyExistsException;
import org.solovyev.android.messenger.realms.RealmBuilder;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.text.Strings;

import java.util.List;

public class XmppRealmConfigurationFragment extends RoboSherlockFragment {

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View result = ViewFromLayoutBuilder.newInstance(R.layout.mpp_realm_conf_xmpp).build(this.getActivity());

        MessengerApplication.getMultiPaneManager().fillContentPane(this.getActivity(), container, result);

        result.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return result;
    }

    @Override
    public void onViewCreated(View root, Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        serverEditText = (EditText) root.findViewById(R.id.mpp_xmpp_server_edittext);
        loginEditText = (EditText) root.findViewById(R.id.mpp_xmpp_login_edittext);
        passwordEditText = (EditText) root.findViewById(R.id.mpp_xmpp_password_edittext);
        resourceEditText = (EditText) root.findViewById(R.id.mpp_xmpp_resource_edittext);

        backButton = (Button) root.findViewById(R.id.mpp_xmpp_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
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
            final RealmBuilder realmBuilder = realmDef.newRealmBuilder(configuration);
            new AsynRealmSaver(this.getActivity(), realmService).execute(realmBuilder);
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

    private static class AsynRealmSaver extends MessengerAsyncTask<RealmBuilder, Integer, Void> {

        @NotNull
        private final RealmService realmService;

        private AsynRealmSaver(@NotNull Activity context, @NotNull RealmService realmService) {
            super(context, true);
            this.realmService = realmService;
        }

        @Override
        protected Void doWork(@NotNull List<RealmBuilder> realmBuilders) {
            for (RealmBuilder realmBuilder : realmBuilders) {
                try {
                    realmService.addRealm(realmBuilder);
                } catch (InvalidCredentialsException e) {
                    throwException(e);
                } catch (RealmAlreadyExistsException e) {
                    throwException(e);
                }
            }

            return null;
        }

        @Override
        protected void onSuccessPostExecute(@Nullable Void result) {
            final Context context = getContext();
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        }

        @Override
        protected void onFailurePostExecute(@NotNull Exception e) {
            if (e instanceof InvalidCredentialsException) {
                Toast.makeText(getContext(), "Invalid credentials!", Toast.LENGTH_SHORT).show();
                Log.e("XmppRealm", e.getMessage(), e);
            } else if (e instanceof RealmAlreadyExistsException) {
                Toast.makeText(getContext(), "Same account alraedy configured!", Toast.LENGTH_SHORT).show();
                Log.e("XmppRealm", e.getMessage(), e);
            } else {
                super.onFailurePostExecute(e);
            }
        }


    }
}
