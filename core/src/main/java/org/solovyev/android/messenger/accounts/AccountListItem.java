package org.solovyev.android.messenger.accounts;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.view.AbstractMessengerListItem;
import org.solovyev.android.messenger.view.ViewAwareTag;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AccountListItem extends AbstractMessengerListItem<Account> {

	@Nonnull
	private static final String TAG_PREFIX = "account_list_item_";

    /*
	**********************************************************************
    *
    *                           VIEWS
    *
    **********************************************************************
    */


	public AccountListItem(@Nonnull Account account) {
		super(TAG_PREFIX, account, R.layout.mpp_list_item_realm);
	}

	@Nullable
	@Override
	public OnClickAction getOnClickAction() {
		return new OnClickAction() {
			@Override
			public void onClick(@Nonnull Context context, @Nonnull ListAdapter<? extends ListItem> adapter, @Nonnull ListView listView) {
				final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
				eventManager.fire(AccountGuiEventType.newAccountViewRequestedEvent(getRealm()));
			}
		};
	}

	@Nonnull
	private Account getRealm() {
		return getData();
	}

	@Nullable
	@Override
	public OnClickAction getOnLongClickAction() {
		return null;
	}

	public void onRealmChangedEvent(@Nonnull Account eventAccount) {
		final Account account = getRealm();
		if (account.equals(eventAccount)) {
			setData(eventAccount);
		}
	}

	@Nonnull
	@Override
	protected String getDisplayName(@Nonnull Account account, @Nonnull Context context) {
		return account.getUser().getDisplayName();
	}

	@Override
	protected void fillView(@Nonnull Account account, @Nonnull Context context, @Nonnull ViewAwareTag viewTag) {
		final ImageView realmIconImageView = viewTag.getViewById(R.id.mpp_li_realm_icon_imageview);

		final Drawable realmIcon = context.getResources().getDrawable(account.getRealmDef().getIconResId());
		realmIconImageView.setImageDrawable(realmIcon);

		final TextView realmUserNameTextView = viewTag.getViewById(R.id.mpp_li_realm_user_name_textview);
		realmUserNameTextView.setText(getDisplayName());

		final TextView realmNameTextView = viewTag.getViewById(R.id.mpp_li_realm_name_textview);
		realmNameTextView.setText(account.getDisplayName(context));

		final View realmWarningView = viewTag.getViewById(R.id.mpp_li_realm_warning_imageview);
		if (account.isEnabled()) {
			realmWarningView.setVisibility(View.GONE);
		} else {
			realmWarningView.setVisibility(View.VISIBLE);
		}
	}
}
