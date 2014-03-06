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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.messenger.BaseFragment;
import org.solovyev.android.messenger.EntityAwareByIdFinder;
import org.solovyev.android.messenger.MultiPaneManager;
import org.solovyev.android.messenger.UiThreadEventListener;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.solovyev.android.messenger.users.ContactFragment.createContactHeaderView;

public class ContactsInfoFragment extends BaseFragment {

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

	@Nullable
	private JEventListener<UserEvent> userEventListener;

	private boolean updateUiOnResume = false;

	public ContactsInfoFragment() {
		super(R.layout.mpp_fragment_contacts);
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		userEventListener = UiThreadEventListener.onUiThread(this, new UserEventListener());
		userService.addListener(userEventListener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = super.onCreateView(inflater, container, savedInstanceState);

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

		updateUi(root);
	}

	private void updateUi(@Nonnull View root) {
		final ViewGroup contactsView = (ViewGroup) root.findViewById(R.id.mpp_contacts_viewgroup);
		contactsView.removeAllViews();

		final Context context = getThemeContext();
		final ViewFromLayoutBuilder<View> propertyDividerBuilder = ViewFromLayoutBuilder.newInstance(R.layout.mpp_property_divider);
		for (final User contact : contacts) {
			final View view = createContactHeaderView(contact, null, context);
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					getEventManager().fire(new ContactUiEvent.Open(contact));
				}
			});
			contactsView.addView(view);
			contactsView.addView(propertyDividerBuilder.build(context));
			// todo serso: add spacing between items?
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (updateUiOnResume) {
			updateUi(getView());
			updateUiOnResume = false;
		}
	}

	private void onContactsUpdated() {
		final View view = getView();
		if (view != null) {
			updateUi(view);
		} else {
			updateUiOnResume = true;
		}
	}

	@Override
	public void onDestroy() {
		if (userEventListener != null) {
			userService.removeListener(userEventListener);
			userEventListener = null;
		}

		super.onDestroy();
	}

	@Nullable
	@Override
	protected CharSequence getFragmentTitle() {
		return getString(R.string.mpp_chat_participants);
	}

	private class UserEventListener extends AbstractJEventListener<UserEvent> {

		protected UserEventListener() {
			super(UserEvent.class);
		}

		@Override
		public void onEvent(@Nonnull UserEvent event) {
			final User user = event.getUser();

			boolean contactsUpdated = false;

			if (contacts != null && !contacts.isEmpty()) {
				switch (event.getType()) {
					case changed:
						contactsUpdated |= onContactChanged(user);
						break;
					case contacts_changed:
						for (User contact : event.getDataAsUsers()) {
							contactsUpdated |= onContactChanged(contact);
						}
						break;
					case contact_removed:
						final String contactId = event.getDataAsUserId();
						contactsUpdated |= Iterables.removeIf(contacts, new EntityAwareByIdFinder(contactId));
						break;
				}
			}

			if(contactsUpdated) {
				onContactsUpdated();
			}
		}

		private boolean onContactChanged(User user) {
			final int index = contacts.indexOf(user);
			if (index >= 0) {
				contacts.set(index, user);
				return true;
			} else {
				return false;
			}
		}
	}
}

