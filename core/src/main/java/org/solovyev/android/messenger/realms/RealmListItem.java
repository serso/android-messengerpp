package org.solovyev.android.messenger.realms;

import android.content.Context;
import android.graphics.drawable.Drawable;
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

class RealmListItem extends AbstractMessengerListItem<Realm> {

	@Nonnull
	private static final String TAG_PREFIX = "realm_list_item_";

	RealmListItem(@Nonnull Realm realm) {
		super(TAG_PREFIX, realm, R.layout.mpp_list_item_realm);
	}

	@Nullable
	@Override
	public OnClickAction getOnClickAction() {
		return new OnClickAction() {
			@Override
			public void onClick(@Nonnull Context context, @Nonnull ListAdapter<? extends ListItem> adapter, @Nonnull ListView listView) {
				final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
				eventManager.fire(RealmUiEventType.newRealmClickedEvent(getRealmDef()));
			}
		};
	}

	@Nonnull
	private Realm getRealmDef() {
		return getData();
	}

	@Nullable
	@Override
	public OnClickAction getOnLongClickAction() {
		return null;
	}

	@Nonnull
	@Override
	protected String getDisplayName(@Nonnull Realm realm, @Nonnull Context context) {
		return context.getString(realm.getNameResId());
	}

	@Override
	protected void fillView(@Nonnull Realm realm, @Nonnull Context context, @Nonnull ViewAwareTag viewTag) {
		final ImageView iconImageView = viewTag.getViewById(R.id.mpp_li_realm_icon_imageview);
		final Drawable configuredRealmIcon = context.getResources().getDrawable(realm.getIconResId());
		iconImageView.setImageDrawable(configuredRealmIcon);

		final TextView nameTextView = viewTag.getViewById(R.id.mpp_li_realm_name_textview);
		nameTextView.setText(getDisplayName());
	}
}
