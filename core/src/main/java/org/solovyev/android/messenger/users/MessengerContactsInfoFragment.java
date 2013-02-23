package org.solovyev.android.messenger.users;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.common.base.Splitter;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.Views;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.R;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.view.ViewFromLayoutBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 8/19/12
 * Time: 5:52 PM
 */
public class MessengerContactsInfoFragment extends RoboSherlockFragment {

    @Inject
    @NotNull
    private UserService userService;

    @Inject
    @NotNull
    private Realm realm;

    @NotNull
    private static final String CONTACT_IDS = "contact_ids";

    private List<User> contacts;
    private Iterable<String> contactIds;

    public MessengerContactsInfoFragment() {
    }

    public MessengerContactsInfoFragment(@NotNull Iterable<String> contactIds) {
        this.contactIds = contactIds;
    }

    public MessengerContactsInfoFragment(@NotNull List<User> contacts) {
        this.contacts = contacts;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View result = ViewFromLayoutBuilder.newInstance(R.layout.msg_contacts).build(this.getActivity());

        MessengerApplication.getMultiPaneManager().fillContentPane(this.getActivity(), container, result);

        result.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return result;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // restore state
        if (contacts == null) {
            if ( contactIds == null ) {
                final String contactIdsString = savedInstanceState.getString(CONTACT_IDS);
                if ( contactIdsString != null ) {
                    contactIds = Splitter.on(';').split(contactIdsString);
                }
            }

            if ( contactIds != null ) {
                contacts = new ArrayList<User>();
                for (String contactId : contactIds) {
                    contacts.add(userService.getUserById(contactId, this.getActivity()));
                }
            }

            if (contacts == null) {
                Log.e(getClass().getSimpleName(), "Contact is null and no data is stored in bundle");
                getActivity().finish();
            }
        }

        final ViewGroup root = (ViewGroup)getView().findViewById(R.id.contacts_container);

        final boolean portrait = Views.getScreenOrientation(this.getActivity()) == Configuration.ORIENTATION_PORTRAIT;

        ViewGroup contactsRow = null;
        for (int i = 0; i < contacts.size(); i++) {
            final User contact = contacts.get(i);

            final LinearLayout contactContainer;
            if ( i % 2 == 0 ) {
                contactsRow = ViewFromLayoutBuilder.<ViewGroup>newInstance(R.layout.msg_contacts_row).build(this.getActivity());
                root.addView(contactsRow);
                contactContainer = (LinearLayout) contactsRow.findViewById(R.id.left_contact_container);
            } else {
                contactContainer = (LinearLayout) contactsRow.findViewById(R.id.right_contact_container);
            }

            if ( portrait ) {
                contactContainer.setOrientation(LinearLayout.HORIZONTAL);
            } else {
                contactContainer.setOrientation(LinearLayout.VERTICAL);
            }

            final TextView contactName = (TextView) contactContainer.findViewById(R.id.contact_name);
            contactName.setText(contact.getDisplayName());

            final ImageView contactIcon = (ImageView) contactContainer.findViewById(R.id.contact_icon);
            MessengerApplication.getServiceLocator().getUserService().setUserPhoto(contactIcon, contact, getActivity());

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        final StringBuilder sb = new StringBuilder();
        for (User contact : contacts) {
            sb.append(contact.getId()).append(";");
        }
        outState.putString(CONTACT_IDS, sb.toString());
    }
}

