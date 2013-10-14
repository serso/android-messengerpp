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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.Views;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.MultiPaneManager;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.view.ViewFromLayoutBuilder;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.common.base.Splitter;
import com.google.inject.Inject;

/**
 * User: serso
 * Date: 8/19/12
 * Time: 5:52 PM
 */
public class ContactsInfoFragment extends RoboSherlockFragment {

	@Nonnull
	public static final String FRAGMENT_TAG = "contacts-info";

	@Inject
	@Nonnull
	private UserService userService;

	@Inject
	@Nonnull
	private MultiPaneManager multiPaneManager;

	@Inject
	@Nonnull
	private Realm realm;

	@Nonnull
	private static final String CONTACT_IDS = "contact_ids";

	private List<User> contacts;
	private Iterable<String> contactIds;

	public ContactsInfoFragment() {
	}

	public ContactsInfoFragment(@Nonnull Iterable<String> contactIds) {
		this.contactIds = contactIds;
	}

	public ContactsInfoFragment(@Nonnull List<User> contacts) {
		this.contacts = contacts;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View result = ViewFromLayoutBuilder.newInstance(R.layout.mpp_contacts).build(this.getActivity());

		multiPaneManager.onCreatePane(this.getActivity(), container, result);

		result.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		return result;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// restore state
		if (contacts == null) {
			if (contactIds == null) {
				final String contactIdsString = savedInstanceState.getString(CONTACT_IDS);
				if (contactIdsString != null) {
					contactIds = Splitter.on(';').split(contactIdsString);
				}
			}

			if (contactIds != null) {
				contacts = new ArrayList<User>();
				for (String contactId : contactIds) {
					contacts.add(userService.getUserById(Entities.newEntityFromEntityId(contactId)));
				}
			}

			if (contacts == null) {
				Log.e(getClass().getSimpleName(), "Contact is null and no data is stored in bundle");
				getActivity().finish();
			}
		}

		final ViewGroup root = (ViewGroup) getView().findViewById(R.id.contacts_container);

		final boolean portrait = Views.getScreenOrientation(this.getActivity()) == Configuration.ORIENTATION_PORTRAIT;

		ViewGroup contactsRow = null;
		for (int i = 0; i < contacts.size(); i++) {
			final User contact = contacts.get(i);

			final LinearLayout contactContainer;
			if (i % 2 == 0) {
				contactsRow = ViewFromLayoutBuilder.<ViewGroup>newInstance(R.layout.mpp_contacts_row).build(this.getActivity());
				root.addView(contactsRow);
				contactContainer = (LinearLayout) contactsRow.findViewById(R.id.left_contact_container);
			} else {
				contactContainer = (LinearLayout) contactsRow.findViewById(R.id.right_contact_container);
			}

			if (portrait) {
				contactContainer.setOrientation(LinearLayout.HORIZONTAL);
			} else {
				contactContainer.setOrientation(LinearLayout.VERTICAL);
			}

			final TextView contactName = (TextView) contactContainer.findViewById(R.id.mpp_contact_name_textview);
			contactName.setText(contact.getDisplayName());

			final ImageView contactIcon = (ImageView) contactContainer.findViewById(R.id.mpp_contact_icon_imageview);
			App.getUserService().getIconsService().setUserPhoto(contact, contactIcon);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		final StringBuilder sb = new StringBuilder();
		for (User contact : contacts) {
			sb.append(contact.getEntity().getEntityId()).append(";");
		}
		outState.putString(CONTACT_IDS, sb.toString());
	}
}

