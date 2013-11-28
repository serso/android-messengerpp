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

package org.solovyev.android.messenger.preferences;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.view.BaseMessengerListItem;
import org.solovyev.android.messenger.view.ViewAwareTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.App.getEventManager;

public final class PreferenceGroupListItem extends BaseMessengerListItem<PreferenceGroup> {

	@Nonnull
	private static final String TAG_PREFIX = "preference_group_list_item_";

	public PreferenceGroupListItem(@Nonnull PreferenceGroup preferenceGroup) {
		super(TAG_PREFIX, preferenceGroup, R.layout.mpp_list_item_preference);
	}

	@Nonnull
	@Override
	protected CharSequence getDisplayName(@Nonnull PreferenceGroup preferenceGroup, @Nonnull Context context) {
		return context.getString(preferenceGroup.getNameResId());
	}

	@Override
	protected void fillView(@Nonnull PreferenceGroup preferenceGroup, @Nonnull Context context, @Nonnull ViewAwareTag viewTag) {
		final ImageView preferenceIconImageView = viewTag.getViewById(R.id.mpp_li_preference_icon_imageview);

		if (preferenceGroup.hasIcon()) {
			preferenceIconImageView.setImageDrawable(context.getResources().getDrawable(preferenceGroup.getIconResId()));
		} else {
			preferenceIconImageView.setImageDrawable(null);
		}

		final TextView preferenceNameTextView = viewTag.getViewById(R.id.mpp_li_preference_name_textview);
		preferenceNameTextView.setText(getDisplayName());
	}

	@Nullable
	@Override
	public OnClickAction getOnClickAction() {
		return new OnClickAction() {
			@Override
			public void onClick(@Nonnull Context context, @Nonnull ListAdapter<? extends ListItem> adapter) {
				getEventManager(context).fire(new PreferenceUiEvent.Clicked(getData()));
			}
		};
	}

	@Nullable
	@Override
	public OnClickAction getOnLongClickAction() {
		return null;
	}

	@Nonnull
	public PreferenceGroup getPreferenceGroup() {
		return getData();
	}
}
