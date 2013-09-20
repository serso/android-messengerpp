package org.solovyev.android.messenger.realms;

import android.content.Context;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.solovyev.android.messenger.MessengerListItemAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class RealmsAdapter extends MessengerListItemAdapter<RealmListItem> {

	public RealmsAdapter(@Nonnull Context context, @Nonnull List<? extends RealmListItem> listItems) {
		super(context, listItems);
	}

    /*
	**********************************************************************
    *
    *                           REALM LISTENERS
    *
    **********************************************************************
    */

	public void onRealmEvent(@Nonnull AccountEvent accountEvent) {
		final Account account = accountEvent.getRealm();
		switch (accountEvent.getType()) {
			case created:
				addListItem(createListItem(account));
				break;
			case changed:
				final RealmListItem listItem = findInAllElements(account);
				if (listItem != null) {
					listItem.onRealmChangedEvent(account);
				}
				break;
			case state_changed:
				switch (account.getState()) {
					case enabled:
					case disabled_by_user:
					case disabled_by_app:
						final RealmListItem realmListItem = findInAllElements(account);
						if (realmListItem != null) {
							realmListItem.onRealmChangedEvent(account);
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
	protected RealmListItem findInAllElements(@Nonnull Account account) {
		return Iterables.find(getAllElements(), Predicates.<RealmListItem>equalTo(createListItem(account)), null);
	}

	@Nonnull
	private RealmListItem createListItem(@Nonnull Account account) {
		return new RealmListItem(account);
	}
}
