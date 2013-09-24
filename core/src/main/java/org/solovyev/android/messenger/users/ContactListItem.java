package org.solovyev.android.messenger.users;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.view.AbstractMessengerListItem;
import org.solovyev.android.messenger.view.ViewAwareTag;

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
				final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
				eventManager.fire(ContactUiEventType.newContactClicked(getContact()));

			}
		};
	}

	@Override
	public OnClickAction getOnLongClickAction() {
		return null;
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
			accountName.setVisibility(View.GONE);
		} else {
			accountName.setVisibility(View.VISIBLE);
			try {
				final Account account = accountService.getAccountById(getContact().getEntity().getAccountId());
				accountName.setText("[" + account.getUser().getDisplayName() + "]");
			} catch (UnsupportedAccountException e) {
				// cannot do anything => just handle exception
				App.getExceptionHandler().handleException(e);
			}
		}

		final View contactOnline = viewTag.getViewById(R.id.mpp_li_contact_online_view);
		if (contact.getContact().isOnline()) {
			contactOnline.setVisibility(View.VISIBLE);
		} else {
			contactOnline.setVisibility(View.INVISIBLE);
		}
	}
}
