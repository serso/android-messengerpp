package org.solovyev.android.messenger.accounts;

import android.content.Context;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.solovyev.android.messenger.MessengerListItemAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class AccountsAdapter extends MessengerListItemAdapter<AccountListItem> {

	public AccountsAdapter(@Nonnull Context context, @Nonnull List<? extends AccountListItem> listItems) {
		super(context, listItems);
	}

    /*
	**********************************************************************
    *
    *                           ACCOUNT LISTENERS
    *
    **********************************************************************
    */

	public void onAccountEvent(@Nonnull AccountEvent accountEvent) {
		final Account account = accountEvent.getAccount();
		switch (accountEvent.getType()) {
			case created:
				addListItem(createListItem(account));
				break;
			case changed:
				final AccountListItem listItem = findInAllElements(account);
				if (listItem != null) {
					listItem.onAccountChangedEvent(account);
				}
				break;
			case state_changed:
				switch (account.getState()) {
					case enabled:
					case disabled_by_user:
					case disabled_by_app:
						final AccountListItem accountListItem = findInAllElements(account);
						if (accountListItem != null) {
							accountListItem.onAccountChangedEvent(account);
						}
						break;
					case removed:
						removeListItem(createListItem(account));
						break;
				}
				break;
		}
	}

	@Nullable
	protected AccountListItem findInAllElements(@Nonnull Account account) {
		return Iterables.find(getAllElements(), Predicates.<AccountListItem>equalTo(createListItem(account)), null);
	}

	@Nonnull
	private AccountListItem createListItem(@Nonnull Account account) {
		return new AccountListItem(account);
	}
}
