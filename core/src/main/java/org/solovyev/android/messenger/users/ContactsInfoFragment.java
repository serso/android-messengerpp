/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.users;

import android.content.Context;
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
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.MultiPaneManager;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.view.ViewFromLayoutBuilder;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.common.base.Function;
import com.google.inject.Inject;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;

public class ContactsInfoFragment extends RoboSherlockFragment {

	@Nonnull
	public static final String FRAGMENT_TAG = "contacts-info";

	@Inject
	@Nonnull
	private UserService userService;

	@Inject
	@Nonnull
	private MultiPaneManager multiPaneManager;

	@Nonnull
	private static final String ARG_CONTACT_IDS = "contact_ids";

	private List<User> contacts;

	private Iterable<String> contactIds;

	public ContactsInfoFragment() {
	}

	@Nonnull
	public static MultiPaneFragmentDef newViewContactsFragmentDef(@Nonnull Context context, @Nonnull List<User> contacts, boolean addToBackStack) {
		final Bundle arguments = new Bundle();
		arguments.putStringArray(ARG_CONTACT_IDS, transform(contacts, new Function<User, String>() {
			@Override
			public String apply(User user) {
				return user.getId();
			}
		}).toArray(new String[contacts.size()]));

		return MultiPaneFragmentDef.forClass(FRAGMENT_TAG, addToBackStack, ContactsInfoFragment.class, context, arguments);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = ViewFromLayoutBuilder.newInstance(R.layout.mpp_fragment_contacts).build(this.getActivity());

		multiPaneManager.onCreatePane(this.getActivity(), container, root);

		final int padding = this.getActivity().getResources().getDimensionPixelSize(R.dimen.mpp_fragment_padding);
		root.setPadding(padding, padding, padding, padding);

		if (contacts == null) {
			if (this.contactIds == null) {
				final String[] contactIds = getArguments().getStringArray(ARG_CONTACT_IDS);
				if (contactIds != null) {
					this.contactIds = asList(contactIds);
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

		return root;
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final ViewGroup contactsView = (ViewGroup) root.findViewById(R.id.contacts_container);

		final boolean portrait = Views.getScreenOrientation(this.getActivity()) == Configuration.ORIENTATION_PORTRAIT;

		ViewGroup contactsRow = null;
		for (int i = 0; i < this.contacts.size(); i++) {
			final User contact = this.contacts.get(i);

			final LinearLayout contactContainer;
			if (i % 2 == 0) {
				contactsRow = ViewFromLayoutBuilder.<ViewGroup>newInstance(R.layout.mpp_fragment_contacts_row).build(this.getActivity());
				contactsView.addView(contactsRow);
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

		multiPaneManager.showTitle(getSherlockActivity(), this, getString(R.string.mpp_chat_participants));
	}
}

