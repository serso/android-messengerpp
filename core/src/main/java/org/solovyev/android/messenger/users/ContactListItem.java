package org.solovyev.android.messenger.users;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

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

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.users.ContactUiEventType.contact_clicked;
import static org.solovyev.android.messenger.users.ContactUiEventType.edit_contact;

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

	private static int getUnreadMessagesCount(@Nonnull User contact) {
		return App.getUserService().getUnreadMessagesCount(contact.getEntity());
	}

	@Nonnull
	public static ContactListItem newEmpty(@Nonnull User contact) {
		return newInstance(UiContact.newInstance(contact, 0));
	}

	@Nonnull
	public static ContactListItem newInstance(@Nonnull User contact) {
		return new ContactListItem(UiContact.newInstance(contact, getUnreadMessagesCount(contact)));
	}

	@Nonnull
	public static ContactListItem newInstance(@Nonnull UiContact contact) {
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
		menuItems.add(Menu.edit);
		final User contact = getContact();
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
	protected void fillView(@Nonnull UiContact contact, @Nonnull Context context, @Nonnull ViewAwareTag viewTag) {
		final ImageView contactIcon = viewTag.getViewById(R.id.mpp_li_contact_icon_imageview);
		App.getUserService().setUserIcon(contact.getContact(), contactIcon);

		final TextView contactName = viewTag.getViewById(R.id.mpp_li_contact_name_textview);
		contactName.setText(getDisplayName());

		final AccountService accountService = App.getAccountService();

		final TextView accountName = viewTag.getViewById(R.id.mpp_li_contact_account_textview);
		if (accountService.isOneAccount()) {
			accountName.setVisibility(GONE);
		} else {
			accountName.setVisibility(VISIBLE);
			try {
				final Account account = accountService.getAccountById(getContact().getEntity().getAccountId());
				accountName.setText("[" + account.getDisplayName(context) + "/" + account.getUser().getDisplayName() + "]");
			} catch (UnsupportedAccountException e) {
				// cannot do anything => just handle exception
				App.getExceptionHandler().handleException(e);
			}
		}

		final View contactOnline = viewTag.getViewById(R.id.mpp_li_contact_online_view);
		if (contact.getContact().isOnline()) {
			contactOnline.setVisibility(VISIBLE);
		} else {
			contactOnline.setVisibility(INVISIBLE);
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
