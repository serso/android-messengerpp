package org.solovyev.android.messenger.users;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerMultiPaneManager;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.view.ViewFromLayoutBuilder;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * User: serso
 * Date: 8/17/12
 * Time: 1:22 AM
 */
public class MessengerContactFragment extends RoboSherlockFragment {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

    @Nonnull
    private static final String CONTACT = "contact";


    @Inject
    @Nonnull
    private UserService userService;

    @Inject
    @Nonnull
    private RealmService realmService;

    @Inject
    @Nonnull
    private MessengerMultiPaneManager multiPaneManager;

    private User contact;

    private RealmEntity realmContact;


    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

    public MessengerContactFragment() {
        // will be loaded later by id from fragment arguments
        this.contact = null;
        this.realmContact = null;
    }

    public MessengerContactFragment(@Nonnull User contact) {
        this.contact = contact;
        this.realmContact = contact.getRealmUser();
    }

    public MessengerContactFragment(@Nonnull RealmEntity realmContact) {
        // will be loaded later by realmContact
        this.contact = null;
        this.realmContact = realmContact;
    }


    @Nonnull
    public static MessengerContactFragment newForContact(@Nonnull User contact) {
        final MessengerContactFragment result = new MessengerContactFragment(contact);
        fillArguments(contact.getRealmUser(), result);
        return result;
    }

    @Nonnull
    public static MessengerContactFragment newForContact(@Nonnull RealmEntity realmContact) {
        final MessengerContactFragment result = new MessengerContactFragment(realmContact);
        fillArguments(realmContact, result);
        return result;
    }

    private static void fillArguments(@Nonnull RealmEntity realmUser, @Nonnull MessengerContactFragment result) {
        final Bundle args = new Bundle();
        args.putParcelable(CONTACT, realmUser);
        result.setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (contact == null) {
            // restore state
            final RealmEntity realmContact;

            if (this.realmContact != null) {
                realmContact = this.realmContact;
            } else {
                realmContact = (RealmEntity) getArguments().getParcelable(CONTACT);
            }

            if (realmContact != null) {
                this.contact = this.userService.getUserById(realmContact);
                this.realmContact = realmContact;
            } else {
                getActivity().finish();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View result = ViewFromLayoutBuilder.newInstance(R.layout.msg_contact).build(this.getActivity());

        multiPaneManager.fillContentPane(this.getActivity(), container, result);

        result.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return result;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ViewGroup root = (ViewGroup)getView().findViewById(R.id.contact_container);

        final TextView contactName = (TextView) root.findViewById(R.id.contact_name);
        contactName.setText(contact.getDisplayName());

        final ImageView contactIcon = (ImageView) root.findViewById(R.id.contact_icon);
        MessengerApplication.getServiceLocator().getUserService().setUserPhoto(contactIcon, contact);

        final List<AProperty> contactProperties = realmService.getRealmById(contact.getRealmUser().getRealmId()).getRealmUserService().getUserProperties(contact, this.getActivity());
        for (AProperty contactProperty : contactProperties) {
            final View contactPropertyView = ViewFromLayoutBuilder.newInstance(R.layout.msg_contact_property).build(this.getActivity());

            final TextView propertyLabel = (TextView) contactPropertyView.findViewById(R.id.property_label);
            propertyLabel.setText(contactProperty.getName());

            final TextView propertyValue = (TextView) contactPropertyView.findViewById(R.id.property_value);
            propertyValue.setText(contactProperty.getValue());

            root.addView(contactPropertyView);
        }
    }

    @Nonnull
    public RealmEntity getContact() {
        return realmContact;
    }
}
