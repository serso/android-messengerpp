package org.solovyev.android.messenger.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import org.solovyev.android.list.ListItemAdapter;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ViewBuilder;
import org.solovyev.common.msg.Message;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class NotificationsViewBuilder implements ViewBuilder<View> {

	@Nonnull
	private final List<Message> notifications;

	public NotificationsViewBuilder(@Nonnull List<Message> notifications) {
		this.notifications = notifications;
	}

	@Nonnull
	@Override
	public View build(@Nonnull Context context) {
		final LayoutInflater li = LayoutInflater.from(context);
		final View root = li.inflate(R.layout.mpp_popup_notifications, null);

		final ListView listView = (ListView) root.findViewById(android.R.id.list);

		final List<NotificationListItem> notificationListItems = new ArrayList<NotificationListItem>();
		for (Message notification : notifications) {
			notificationListItems.add(new NotificationListItem(notification));
		}

		ListItemAdapter.attach(listView, ListItemAdapter.newInstance(context, notificationListItems), context);

		return root;
	}
}
