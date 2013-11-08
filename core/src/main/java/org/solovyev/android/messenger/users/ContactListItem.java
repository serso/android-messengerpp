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
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.list.ListItemOnClickData;
import org.solovyev.android.list.SimpleMenuOnClick;
import org.solovyev.android.menu.LabeledMenuItem;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.view.AbstractMessengerListItem;
import org.solovyev.android.messenger.view.ViewAwareTag;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.*;
import static org.solovyev.android.messenger.App.getAccountService;
import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.users.ContactUiEventType.*;
import static org.solovyev.android.messenger.users.UiContact.loadUiContact;
import static org.solovyev.android.messenger.users.UiContact.newUiContact;
import static org.solovyev.android.messenger.users.Users.fillContactPresenceViews;

public final class ContactListItem extends AbstractMessengerListItem<UiContact> {

	@Nonnull
	private static final String TAG_PREFIX = "contact_list_item_";

	private ContactListItem(@Nonnull UiContact contact) {
		super(TAG_PREFIX, contact, R.layout.mpp_list_item_contact);
		setDisplayName(contact.getDisplayName());
	}

	@Nonnull
	public static ContactListItem newEmpty(@Nonnull User contact) {
		return newContactListItem(newUiContact(contact, 0, null));
	}

	@Nonnull
	public static ContactListItem loadContactListItem(@Nonnull User contact) {
		return new ContactListItem(loadUiContact(contact));
	}

	@Nonnull
	public static ContactListItem newContactListItem(@Nonnull UiContact contact) {
		return new ContactListItem(contact);
	}

	@Override
	public OnClickAction getOnClickAction() {
		return new OnClickAction() {
			@Override
			public void onClick(@Nonnull final Context context, @Nonnull final ListAdapter<? extends ListItem> adapter, @Nonnull ListView listView) {
				getEventManager(context).fire(contact_clicked.newEvent(getContact()));
			}
		};
	}

	@Override
	public OnClickAction getOnLongClickAction() {
		final List<LabeledMenuItem<ListItemOnClickData<User>>> menuItems = new ArrayList<LabeledMenuItem<ListItemOnClickData<User>>>();
		final User contact = getContact();
		for (Menu menuItem : Menu.values()) {
			if(menuItem.isVisible(getData())) {
				menuItems.add(menuItem);
			}
		}

		return new SimpleMenuOnClick<User>(menuItems, contact, "contact-menu");
	}

	public boolean onUnreadMessagesCountChanged(@Nonnull Integer unreadMessagesCount) {
		boolean changed = false;

		final UiContact uiContact = getData();
		if (uiContact.getUnreadMessagesCount() != unreadMessagesCount) {
			setData(uiContact.copyForNewUnreadMessagesCount(unreadMessagesCount));
			changed = true;
		}

		return changed;
	}

	public boolean onContactPresenceChanged(@Nonnull User contact) {
		boolean changed = false;

		final UiContact uiContact = getData();
		if (uiContact.getContact().isOnline() != contact.isOnline()) {
			setData(uiContact.copyForNewUser(contact));
			changed = true;
		}

		return changed;
	}

	public void onContactChanged(@Nonnull User newContact) {
		setData(getData().copyForNewUser(newContact));
	}

	@Nonnull
	public User getContact() {
		return getData().getContact();
	}

	@Nonnull
	@Override
	protected CharSequence getDisplayName(@Nonnull UiContact contact, @Nonnull Context context) {
		String displayName = contact.getContact().getDisplayName();
		if (contact.getUnreadMessagesCount() > 0) {
			displayName += " (" + contact.getUnreadMessagesCount() + ")";
		}
		return displayName;
	}

	@Override
	protected void fillView(@Nonnull final UiContact uiContact, @Nonnull final Context context, @Nonnull ViewAwareTag viewTag) {
		final User contact = uiContact.getContact();

		final ImageView contactIcon = viewTag.getViewById(R.id.mpp_li_contact_icon_imageview);
		App.getUserService().getIconsService().setUserIcon(contact, contactIcon);

		final TextView contactName = viewTag.getViewById(R.id.mpp_li_contact_name_textview);
		contactName.setText(getDisplayName());
		if (uiContact.getUnreadMessagesCount() > 0) {
			contactName.setTypeface(null, Typeface.BOLD);
		} else {
			contactName.setTypeface(null, Typeface.NORMAL);
		}

		final AccountService accountService = getAccountService();
		final Account account = uiContact.getAccount();

		final TextView accountName = viewTag.getViewById(R.id.mpp_li_contact_account_textview);
		if (accountService.isOneAccount()) {
			accountName.setVisibility(GONE);
		} else {
			accountName.setVisibility(VISIBLE);
			if (account != null) {
				accountName.setText("[" + account.getDisplayName(context) + "/" + account.getUser().getDisplayName() + "]");
			}
		}

		fillContactPresenceViews(context, viewTag, contact, account);
	}

	/*
	**********************************************************************
	*
	*                           STATIC/INNER
	*
	**********************************************************************
	*/

	private enum Menu implements LabeledMenuItem<ListItemOnClickData<User>> {
		mark_all_read("Mark all read") {
			@Override
			public void onClick(@Nonnull ListItemOnClickData<User> data, @Nonnull Context context) {
				final User contact = data.getDataObject();
				getEventManager(context).fire(mark_all_messages_read.newEvent(contact));
			}

			@Override
			protected boolean isVisible(@Nonnull UiContact uiContact) {
				return uiContact.getUnreadMessagesCount() > 0;
			}
		};

		@Nonnull
		private final String caption;

		Menu(@Nonnull String caption) {
			this.caption = caption;
		}

		@Nonnull
		@Override
		public String getCaption(@Nonnull Context context) {
			return caption;
		}

		protected abstract boolean isVisible(@Nonnull UiContact uiContact);
	}
}
