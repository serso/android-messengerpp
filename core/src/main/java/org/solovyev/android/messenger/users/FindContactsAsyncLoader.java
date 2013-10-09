package org.solovyev.android.messenger.users;

import android.content.Context;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.App;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.users.ContactListItem.newContactListItem;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 3:12 PM
 */
final class FindContactsAsyncLoader extends AbstractAsyncLoader<UiContact, ContactListItem> {

	@Nullable
	private String query;
	private final int maxCount;

	FindContactsAsyncLoader(@Nonnull Context context,
							@Nonnull ListAdapter<ContactListItem> adapter,
							@Nullable Runnable onPostExecute,
							@Nullable String query,
							int maxCount) {
		super(context, adapter, onPostExecute);
		this.query = query;
		this.maxCount = maxCount;
	}

	@Nonnull
	protected List<UiContact> getElements(@Nonnull Context context) {
		return App.getUserService().findContacts(query, maxCount);
	}

	@Nonnull
	@Override
	protected ContactListItem createListItem(@Nonnull UiContact uiContact) {
		return newContactListItem(uiContact);
	}
}
