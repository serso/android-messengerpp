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
		super(TAG_PREFIX, account, R.layout.mpp_list_item_account);
	}

	@Nullable
	@Override
	public OnClickAction getOnClickAction() {
		return new OnClickAction() {
			@Override
			public void onClick(@Nonnull Context context, @Nonnull ListAdapter<? extends ListItem> adapter, @Nonnull ListView listView) {
				final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
				eventManager.fire(AccountUiEventType.newAccountViewRequestedEvent(getRealm()));
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

	public void onAccountChangedEvent(@Nonnull Account eventAccount) {
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
		final ImageView iconImageView = viewTag.getViewById(R.id.mpp_li_account_icon_imageview);

		final Drawable realmIcon = context.getResources().getDrawable(account.getRealm().getIconResId());
		iconImageView.setImageDrawable(realmIcon);

		final TextView userNameTextView = viewTag.getViewById(R.id.mpp_li_account_user_name_textview);
		userNameTextView.setText(getDisplayName());

		final TextView nameTextView = viewTag.getViewById(R.id.mpp_li_account_name_textview);
		nameTextView.setText(account.getDisplayName(context));

		final View warningView = viewTag.getViewById(R.id.mpp_li_account_warning_imageview);
		if (account.isEnabled()) {
			warningView.setVisibility(View.GONE);
		} else {
			warningView.setVisibility(View.VISIBLE);
		}
	}
}
