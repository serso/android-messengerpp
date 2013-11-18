/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.realms;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.view.AbstractMessengerListItem;
import org.solovyev.android.messenger.view.ViewAwareTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.realms.RealmUiEventType.realm_clicked;

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
			public void onClick(@Nonnull Context context, @Nonnull ListAdapter<? extends ListItem> adapter) {
				getEventManager(context).fire(realm_clicked.newEvent(getRealm()));
			}
		};
	}

	@Nonnull
	public Realm getRealm() {
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
