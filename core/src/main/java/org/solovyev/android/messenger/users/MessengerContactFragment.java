package org.solovyev.android.messenger.users;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.AProperty;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.R;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.view.ViewFromLayoutBuilder;

import java.util.List;

/**
 * User: serso
 * Date: 8/17/12
 * Time: 1:22 AM
 */
public class MessengerContactFragment extends RoboSherlockFragment {

    @Inject
    @NotNull
    private UserService userService;

    @Inject
    @NotNull
    private Realm realm;

    @NotNull
    private static final String CONTACT_ID = "contact_id";

    private User contact;
    private String contactId;

    public MessengerContactFragment() {
    }

    public MessengerContactFragment(@NotNull User contact) {
        this.contact = contact;
    }

    public MessengerContactFragment(@NotNull String contactId) {
        this.contactId = contactId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View result = ViewFromLayoutBuilder.newInstance(R.layout.msg_contact).build(this.getActivity());

        MessengerApplication.getMultiPaneManager().fillContentPane(this.getActivity(), container, result);

        result.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return result;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // restore state
        if (contact == null) {
            String contactId = this.contactId;
            if ( contactId == null ) {
                contactId = savedInstanceState.getString(CONTACT_ID);
            }

            if (contactId != null) {
                contact = this.userService.getUserById(contactId, getActivity());
            }

            if (contact == null) {
                Log.e(getClass().getSimpleName(), "Contact is null and no data is stored in bundle");
                getActivity().finish();
            }
        }

        final ViewGroup root = (ViewGroup)getView().findViewById(R.id.contact_container);

        final TextView contactName = (TextView) root.findViewById(R.id.contact_name);
        contactName.setText(contact.getDisplayName());

        final ImageView contactIcon = (ImageView) root.findViewById(R.id.contact_icon);
        MessengerApplication.getServiceLocator().getUserService().setUserPhoto(contactIcon, contact, getActivity());

        final List<AProperty> contactProperties = realm.getRealmUserService().getUserProperties(contact, this.getActivity());
        for (AProperty contactProperty : contactProperties) {
            final View contactPropertyView = ViewFromLayoutBuilder.newInstance(R.layout.msg_contact_property).build(this.getActivity());

            final TextView propertyLabel = (TextView) contactPropertyView.findViewById(R.id.property_label);
            propertyLabel.setText(contactProperty.getName());

            final TextView propertyValue = (TextView) contactPropertyView.findViewById(R.id.property_value);
            propertyValue.setText(contactProperty.getValue());

            root.addView(contactPropertyView);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(CONTACT_ID, contact.getId());
    }
}
