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

package org.solovyev.android.messenger.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.list.ListItemAdapter;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ViewBuilder;

public final class NotificationsViewBuilder implements ViewBuilder<View> {

	@Nonnull
	private final List<Notification> notifications;

	public NotificationsViewBuilder(@Nonnull List<Notification> notifications) {
		this.notifications = notifications;
	}

	@Nonnull
	@Override
	public View build(@Nonnull Context context) {
		final LayoutInflater li = LayoutInflater.from(context);
		final View root = li.inflate(R.layout.mpp_popup_notifications, null);

		final ListView listView = (ListView) root.findViewById(android.R.id.list);

		final List<NotificationListItem> notificationListItems = new ArrayList<NotificationListItem>();
		for (Notification notification : notifications) {
			notificationListItems.add(new NotificationListItem(notification));
		}

		ListItemAdapter.attach(listView, ListItemAdapter.newInstance(context, notificationListItems), context);

		return root;
	}
}
