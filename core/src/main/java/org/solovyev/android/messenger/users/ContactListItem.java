package org.solovyev.android.messenger.users;

import android.content.Context;
import android.view.View;
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
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.view.AbstractMessengerListItem;
import org.solovyev.android.messenger.view.ViewAwareTag;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.*;
import static org.solovyev.android.messenger.App.*;
import static org.solovyev.android.messenger.users.ContactUiEventType.call_contact;
import static org.solovyev.android.messenger.users.ContactUiEventType.contact_clicked;
import static org.solovyev.android.messenger.users.ContactUiEventType.edit_contact;
import static org.solovyev.android.messenger.users.UiContact.loadUiContact;
import static org.solovyev.android.messenger.users.UiContact.newUiContact;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:04 PM
 */
public final class ContactListItem extends AbstractMessengerListItem<UiContact> /*implements UserEventListener*/ {

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
		try {
			final Account account = getAccountService().getAccountByEntity(contact.getEntity());
			if (account.getRealm().canEditUsers()) {
				menuItems.add(Menu.edit);
			}
		} catch (UnsupportedAccountException e) {
			getExceptionHandler().handleException(e);
		}
		return new SimpleMenuOnClick<User>(menuItems, contact, "contact-menu");
	}

	public void onUnreadMessagesCountChanged(@Nonnull Integer unreadMessagesCount) {
		setData(getData().copyForNewUnreadMessagesCount(unreadMessagesCount));
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
		App.getUserService().setUserIcon(contact, contactIcon);

		final TextView contactName = viewTag.getViewById(R.id.mpp_li_contact_name_textview);
		contactName.setText(getDisplayName());

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

		final View contactOnline = viewTag.getViewById(R.id.mpp_li_contact_online_view);
		final View contactCall = viewTag.getViewById(R.id.mpp_li_contact_call_view);
		if (account == null || !account.canCall(contact)) {
			contactCall.setOnClickListener(null);
			contactCall.setVisibility(GONE);

			if (contact.isOnline()) {
				contactOnline.setVisibility(VISIBLE);
			} else {
				contactOnline.setVisibility(INVISIBLE);
			}
		} else {
			contactOnline.setVisibility(GONE);

			// for some reason following properties set from styles xml are not applied => apply them manually
			contactCall.setFocusable(false);
			contactCall.setFocusableInTouchMode(false);

			contactCall.setVisibility(VISIBLE);
			contactCall.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					getEventManager(context).fire(call_contact.newEvent(contact));
				}
			});
		}
	}

	/*
	**********************************************************************
	*
	*                           STATIC/INNER
	*
	**********************************************************************
	*/

	private enum Menu implements LabeledMenuItem<ListItemOnClickData<User>> {
		edit("Edit") {
			@Override
			public void onClick(@Nonnull ListItemOnClickData<User> data, @Nonnull Context context) {
				final User contact = data.getDataObject();
				getEventManager(context).fire(edit_contact.newEvent(contact));
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
	}
}
