package org.solovyev.android.messenger.about;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.view.BaseMessengerListItem;
import org.solovyev.android.messenger.view.ViewAwareTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.solovyev.android.messenger.App.getEventManager;

public class AboutListItem extends BaseMessengerListItem<AboutType> {

	private static final String TAG_PREFIX = "about_";

	protected AboutListItem(@Nonnull AboutType type) {
		super(TAG_PREFIX, type, R.layout.mpp_list_item_about);
	}

	@Nonnull
	@Override
	protected CharSequence getDisplayName(@Nonnull AboutType type, @Nonnull Context context) {
		return context.getString(type.getTitleResId());
	}

	@Override
	protected void fillView(@Nonnull AboutType type, @Nonnull Context context, @Nonnull ViewAwareTag viewTag) {
		final TextView titleTextView = viewTag.getViewById(R.id.mpp_li_about_name_textview);
		titleTextView.setText(type.getTitleResId());

		final ImageView iconImageView = viewTag.getViewById(R.id.mpp_li_about_icon_imageview);
		final int iconResId = type.getIconResId();
		if (iconResId == View.NO_ID) {
			iconImageView.setImageDrawable(null);
			iconImageView.setVisibility(INVISIBLE);
		} else {
			iconImageView.setImageDrawable(context.getResources().getDrawable(iconResId));
			iconImageView.setVisibility(VISIBLE);
		}
	}

	@Nullable
	@Override
	public OnClickAction getOnClickAction() {
		return new OnClickAction() {
			@Override
			public void onClick(@Nonnull Context context, @Nonnull ListAdapter<? extends ListItem> adapter) {
				getEventManager(context).fire(new AboutUiEvent.Clicked(getData()));
			}
		};
	}

	@Nullable
	@Override
	public OnClickAction getOnLongClickAction() {
		return null;
	}
}
