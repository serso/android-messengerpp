package org.solovyev.android.messenger.users;

import android.content.Context;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.App.getUserService;

final class RecentContactsAsyncLoader extends AbstractAsyncLoader<UiContact, ContactListItem> {

	private final int maxCount;

	public RecentContactsAsyncLoader(@Nonnull Context context,
									 @Nonnull ListAdapter<ContactListItem> adapter,
									 @Nullable Runnable onPostExecute,
									 int maxCount) {
		super(context, adapter, onPostExecute);
		this.maxCount = maxCount;
	}

	@Nonnull
	@Override
	protected List<UiContact> getElements(@Nonnull Context context) {
		return getUserService().getLastChatedContacts(maxCount);
	}

	@Nonnull
	@Override
	protected ContactListItem createListItem(@Nonnull UiContact uiContact) {
		return ContactListItem.newInstance(uiContact);
	}
}
