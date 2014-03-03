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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.solovyev.android.Views;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.menu.AMenuItem;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.MainActivity;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.Accounts;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.view.PropertyView;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.ConfirmationDialogBuilder;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.solovyev.android.messenger.App.getUserService;
import static org.solovyev.android.messenger.users.Users.getUserIdFromArguments;
import static org.solovyev.android.messenger.view.PropertyView.newPropertyView;
import static org.solovyev.common.text.Strings.isEmpty;

public class ContactFragment extends BaseUserFragment {

	@Nonnull
	public static final String FRAGMENT_TAG = "contact-info";

	private ActivityMenu<Menu, MenuItem> menu;

	private boolean updateUiOnResume = false;

	public ContactFragment() {
		super(R.layout.mpp_fragment_contact);
	}

	@Nonnull
	public static MultiPaneFragmentDef newViewContactFragmentDef(@Nonnull Context context, @Nonnull Account account, @Nonnull Entity contact, boolean addToBackStack) {
		final Bundle arguments = Users.newUserArguments(account, contact);
		return newViewContactFragmentDef(context, arguments, addToBackStack);
	}

	@Nonnull
	public static MultiPaneFragmentDef newViewContactFragmentDef(@Nonnull Context context, @Nonnull Bundle arguments, boolean addToBackStack) {
		final String accountId = Accounts.getAccountIdFromArguments(arguments);
		final String contactId = getUserIdFromArguments(arguments);

		if (isEmpty(accountId)) {
			throw new IllegalArgumentException("Account id must be provided in arguments");
		}

		if (isEmpty(contactId)) {
			throw new IllegalArgumentException("Contact id must be provided in arguments");
		}

		assert contactId != null;
		return MultiPaneFragmentDef.forClass(FRAGMENT_TAG, addToBackStack, ContactFragment.class, context, arguments, new ContactFragmentReuseCondition(contactId));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public void onViewCreated(@Nonnull View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		updateUi(root);
	}

	private void updateUi(@Nonnull View root) {
		final User contact = getUser();

		final Context context = getThemeContext();
		final Resources resources = context.getResources();

		final ViewGroup propertiesViewGroup = (ViewGroup) root.findViewById(R.id.mpp_contact_properties_viewgroup);
		propertiesViewGroup.removeAllViews();

		final Multimap<String, String> properties = ArrayListMultimap.create();
		for (AProperty property : getAccountService().getUserProperties(contact, context)) {
			properties.put(property.getName(), property.getValue());
		}

		final ViewFromLayoutBuilder<View> propertyDividerBuilder = ViewFromLayoutBuilder.newInstance(R.layout.mpp_property_divider);

		final PropertyView header = newPropertyView(context);

		final ImageView contactIcon = header.getIconView();
		contactIcon.setVisibility(VISIBLE);
		final int iconSize = resources.getDimensionPixelSize(R.dimen.mpp_fragment_icon_size);
		contactIcon.getLayoutParams().height = iconSize;
		contactIcon.getLayoutParams().width = iconSize;
		contactIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
		getUserService().getIconsService().setUserPhoto(contact, contactIcon);

		header.setLabel(contact.getDisplayName());
		header.setValue(Accounts.getAccountName(getAccount()));

		propertiesViewGroup.addView(header.getView());

		propertiesViewGroup.addView(propertyDividerBuilder.build(context));

		boolean first = true;
		for (Map.Entry<String, Collection<String>> entry : properties.asMap().entrySet()) {
			for (String propertyValue : entry.getValue()) {
				if (first) {
					first = false;
				} else {
					propertiesViewGroup.addView(propertyDividerBuilder.build(context));
				}
				propertiesViewGroup.addView(newPropertyView(context)
						.setLabel(entry.getKey())
						.setValue(propertyValue)
						.getView());
			}
		}

		final View actionsDivider = root.findViewById(R.id.mpp_contact_actions_divider);
		actionsDivider.setVisibility(first ? GONE : VISIBLE);

		updateActionsVisibility(root);

		final boolean canEditContact = canEditContact();
		newPropertyView(R.id.mpp_contact_edit, root)
				.setVisibility(canEditContact ? VISIBLE : GONE)
				.setLabel(R.string.mpp_edit)
				.setRightIcon(R.drawable.mpp_ab_edit_light)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						editContact();
					}
				});

		final boolean canRemoveContact = canRemoveContact();
		newPropertyView(R.id.mpp_contact_remove, root)
				.setVisibility(canRemoveContact ? VISIBLE : GONE)
				.setLabel(R.string.mpp_remove)
				.setRightIcon(R.drawable.mpp_ab_remove_light)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						tryRemoveContact();
					}
				});

		final View editRemoveDivider = root.findViewById(R.id.mpp_contact_edit_remove_divider);
		editRemoveDivider.setVisibility(canEditContact && canRemoveContact ? VISIBLE : GONE);
	}

	private boolean updateActionsVisibility(@Nonnull View root) {
		final BaseFragmentActivity activity = getSherlockActivity();

		boolean showActions = true;
		if (activity instanceof MainActivity) {
			showActions = Views.getScreenOrientation(activity) == Configuration.ORIENTATION_LANDSCAPE;
		}

		root.findViewById(R.id.mpp_contact_actions).setVisibility(showActions ? VISIBLE : GONE);

		return showActions;
	}

	private void editContact() {
		if (canEditContact()) {
			getEventManager().fire(new ContactUiEvent.Edit(getUser(), getAccount()));
		}
	}

	private boolean canEditContact() {
		return getAccount().getRealm().canEditUsers();
	}

	private boolean canRemoveContact() {
		final User user = getUser();
		if (user != null) {
			final Account account = getAccount();
			final boolean accountUser = account.getUser().equals(user);
			return !accountUser;
		} else {
			return false;
		}
	}

	private void tryRemoveContact() {
		final ConfirmationDialogBuilder builder = ConfirmationDialogBuilder.newInstance(getSherlockActivity(), "contact-removal-confirmation", R.string.mpp_contact_removal_confirmation);
		builder.setPositiveHandler(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				App.getUserService().removeUser(getUser());
			}
		});
		builder.build().show();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (updateUiOnResume) {
			updateUi(getView());
			updateUiOnResume = false;
		}

		updateActionsVisibility(getView());
	}


	@Override
	protected void onUserChanged(@Nonnull User user) {
		super.onUserChanged(user);

		final View view = getView();
		if (view == null) {
			updateUiOnResume = true;
		} else {
			updateUi(view);
		}
	}

	@Nullable
	@Override
	protected CharSequence getFragmentTitle() {
		return getString(R.string.mpp_contact_info);
	}

		/*
	**********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return this.menu.onOptionsItemSelected(this.getActivity(), item);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		this.menu.onPrepareOptionsMenu(this.getActivity(), menu);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		final List<IdentifiableMenuItem<MenuItem>> menuItems = new ArrayList<IdentifiableMenuItem<MenuItem>>();

		final EditContactMenuItem editContactMenuItem = new EditContactMenuItem();
		menuItems.add(editContactMenuItem);

		final RemoveContactMenuItem removeContactMenuItem = new RemoveContactMenuItem();
		menuItems.add(removeContactMenuItem);

		final OpenChatMenuItem openChatMenuItem = new OpenChatMenuItem();
		menuItems.add(openChatMenuItem);

		this.menu = ListActivityMenu.fromResource(R.menu.mpp_menu_contact, menuItems, SherlockMenuHelper.getInstance(), new JPredicate<AMenuItem<MenuItem>>() {
			@Override
			public boolean apply(@Nullable AMenuItem<MenuItem> menuItem) {
				final Activity activity = getSherlockActivity();
				final boolean showActions = !(activity instanceof MainActivity);

				if (menuItem == editContactMenuItem) {
					return !canEditContact() || !showActions;
				} else if (menuItem == openChatMenuItem) {
					return !(getSherlockActivity() instanceof ContactsActivity);
				} else if (menuItem == removeContactMenuItem) {
					return !canRemoveContact() || !showActions;
				}

				return false;
			}
		});
		this.menu.onCreateOptionsMenu(this.getActivity(), menu);
	}

	private class EditContactMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_edit_contact;
		}

		@Override
		public void onClick(@Nonnull MenuItem menuItem, @Nonnull Context context) {
			editContact();
		}
	}

	private class RemoveContactMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_remove_contact;
		}

		@Override
		public void onClick(@Nonnull MenuItem menuItem, @Nonnull Context context) {
			tryRemoveContact();
		}
	}

	private class OpenChatMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_chat_contact;
		}

		@Override
		public void onClick(@Nonnull MenuItem menuItem, @Nonnull Context context) {
			getEventManager().fire(new ContactUiEvent.OpenChat(getUser(), getAccount()));
		}
	}
}
