package org.solovyev.android.messenger.realms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.MessengerMultiPaneManager;
import org.solovyev.android.messenger.R;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import roboguice.event.EventManager;

/**
 * User: serso
 * Date: 3/1/13
 * Time: 8:57 PM
 */
public class MessengerRealmFragment extends RoboSherlockFragment {


    @NotNull
    public static final String EXTRA_REALM_ID = "realm_id";

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @NotNull
    private RealmService realmService;

    @Inject
    @NotNull
    private MessengerMultiPaneManager multiPaneManager;

    @Inject
    @NotNull
    private EventManager eventManager;

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

    @NotNull
    private Realm realm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            final String realmId = arguments.getString(EXTRA_REALM_ID);
            if (realmId != null) {
                realm = realmService.getRealmById(realmId);
            }
        }

        if (realm == null) {
            // remove fragment
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View result = ViewFromLayoutBuilder.newInstance(R.layout.mpp_realm_fragment).build(this.getActivity());

        multiPaneManager.fillContentPane(this.getActivity(), container, result);

        result.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return result;
    }

    @Override
    public void onViewCreated(@NotNull View root, Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        final ImageView realmIconImageView = (ImageView) root.findViewById(R.id.mpp_realm_icon_imageview);
        realmIconImageView.setImageDrawable(getResources().getDrawable(realm.getRealmDef().getIconResId()));

        final TextView realmNameTextView = (TextView) root.findViewById(R.id.mpp_realm_name_textview);
        realmNameTextView.setText(realm.getDisplayName(getActivity()));

        final Button realmRemoveButton = (Button) root.findViewById(R.id.mpp_realm_remove_button);
         realmRemoveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeRealm();
                }
            });

        final Button realmEditButton = (Button) root.findViewById(R.id.mpp_realm_edit_button);
        realmEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editRealm();
            }
        });
    }

    private void editRealm() {
        eventManager.fire(new EditRealmEvent(realm));
    }

    @NotNull
    public Realm getRealm() {
        return realm;
    }


    private void removeRealm() {
        new AsynRealmRemover(this.getActivity(), realmService).execute(realm);
    }

    public static class EditRealmEvent {

        @NotNull
        private Realm realm;

        private EditRealmEvent(@NotNull Realm realm) {
            this.realm = realm;
        }

        @NotNull
        public Realm getRealm() {
            return realm;
        }
    }
}
