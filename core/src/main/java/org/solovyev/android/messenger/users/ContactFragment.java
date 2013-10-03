package org.solovyev.android.messenger.users;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.MultiPaneManager;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.view.ViewFromLayoutBuilder;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;

import static org.solovyev.android.messenger.App.getUserService;

/**
 * User: serso
 * Date: 8/17/12
 * Time: 1:22 AM
 */
public class ContactFragment extends RoboSherlockFragment {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	@Nonnull
	private static final String CONTACT = "contact";

	@Nonnull
	public static final String FRAGMENT_TAG = "contact-info";

	@Inject
	@Nonnull
	private UserService userService;

	@Inject
	@Nonnull
	private AccountService accountService;

	@Inject
	@Nonnull
	private MultiPaneManager multiPaneManager;

	private User contact;

	private Entity realmContact;


    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

	public ContactFragment() {
		// will be loaded later by id from fragment arguments
		this.contact = null;
		this.realmContact = null;
	}

	public ContactFragment(@Nonnull User contact) {
		this.contact = contact;
		this.realmContact = contact.getEntity();
	}

	public ContactFragment(@Nonnull Entity realmContact) {
		// will be loaded later by realmContact
		this.contact = null;
		this.realmContact = realmContact;
	}


	@Nonnull
	public static ContactFragment newForContact(@Nonnull User contact) {
		final ContactFragment result = new ContactFragment(contact);
		fillArguments(contact.getEntity(), result);
		return result;
	}

	@Nonnull
	public static ContactFragment newForContact(@Nonnull Entity realmContact) {
		final ContactFragment result = new ContactFragment(realmContact);
		fillArguments(realmContact, result);
		return result;
	}

	private static void fillArguments(@Nonnull Entity realmUser, @Nonnull ContactFragment result) {
		final Bundle args = new Bundle();
		args.putParcelable(CONTACT, realmUser);
		result.setArguments(args);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (contact == null) {
			// restore state
			final Entity realmContact;

			if (this.realmContact != null) {
				realmContact = this.realmContact;
			} else {
				realmContact = (Entity) getArguments().getParcelable(CONTACT);
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
		final View result = ViewFromLayoutBuilder.newInstance(R.layout.mpp_fragment_contact).build(this.getActivity());

		multiPaneManager.onCreatePane(this.getActivity(), container, result);

		result.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		return result;
	}

	@Override
	public void onViewCreated(@Nonnull View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final TextView contactName = (TextView) root.findViewById(R.id.mpp_fragment_title);
		contactName.setText(contact.getDisplayName());

		final ImageView contactIcon = (ImageView) root.findViewById(R.id.mpp_contact_icon_imageview);
		getUserService().setUserPhoto(contact, contactIcon);

		final ViewGroup propertiesViewGroup = (ViewGroup) root.findViewById(R.id.mpp_contact_properties_viewgroup);
		final List<AProperty> contactProperties = accountService.getUserProperties(contact, this.getActivity());
		for (AProperty contactProperty : contactProperties) {
			final View propertyView = ViewFromLayoutBuilder.newInstance(R.layout.mpp_property).build(this.getActivity());

			final TextView propertyLabel = (TextView) propertyView.findViewById(R.id.mpp_property_label);
			propertyLabel.setText(contactProperty.getName());

			final TextView propertyValue = (TextView) propertyView.findViewById(R.id.mpp_property_value);
			propertyValue.setText(contactProperty.getValue());

			propertiesViewGroup.addView(propertyView);
		}

		multiPaneManager.onPaneCreated(getActivity(), root);
	}

	@Nonnull
	public Entity getContact() {
		return realmContact;
	}
}
