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

package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import org.solovyev.android.menu.AMenuItem;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.chats.Chats;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.notifications.Notification;
import org.solovyev.android.messenger.notifications.NotificationsViewBuilder;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.APopupWindow;
import org.solovyev.android.view.AbsoluteAPopupWindow;
import org.solovyev.android.view.AnchorAPopupWindow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.App.getUnreadMessagesCounter;

final class MainMenu implements ActivityMenu<Menu, MenuItem> {

	@Nonnull
	private final Runnable onHomeClickListener;

	private ListActivityMenu<Menu, MenuItem> menu;

	MainMenu(@Nonnull Runnable onHomeClickListener) {
		this.onHomeClickListener = onHomeClickListener;
	}

	@Override
	public boolean onPrepareOptionsMenu(@Nonnull Activity activity, @Nonnull Menu menu) {
		boolean result = this.menu.onPrepareOptionsMenu(activity, menu);

		onUnreadMessagesCountChanged(menu, getUnreadMessagesCounter().getUnreadMessagesCount());
		onNewNotificationsAdded(menu, App.getNotificationService().existNotifications());

		return result;
	}

	private void onNewNotificationsAdded(@Nonnull Menu menu, boolean existNotifications) {
		final MenuItem menuItem = menu.findItem(R.id.mpp_menu_notifications);
		final AMenuItem<MenuItem> aMenuItem = this.menu.findMenuItemById(R.id.mpp_menu_notifications);
		if (existNotifications) {
			menuItem.setVisible(true);
			menuItem.setEnabled(true);
			if (aMenuItem instanceof NotificationsMenuItem) {
				((NotificationsMenuItem) aMenuItem).onNotificationsChanged();
			}
		} else {
			menuItem.setVisible(false);
			menuItem.setEnabled(false);
			if (aMenuItem instanceof NotificationsMenuItem) {
				((NotificationsMenuItem) aMenuItem).dismissPopup();
			}
		}
	}

	private void onUnreadMessagesCountChanged(@Nonnull Menu menu, int unreadMessagesCount) {
		final MenuItem menuItem = menu.findItem(R.id.mpp_menu_unread_messages_counter);
		if (unreadMessagesCount == 0) {
			menuItem.setVisible(false);
			menuItem.setEnabled(false);
		} else {
			menuItem.setTitle(String.valueOf(unreadMessagesCount));
			menuItem.setVisible(true);
			menuItem.setEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(@Nonnull Activity activity, @Nonnull Menu menu) {
		if (this.menu == null) {
			final List<IdentifiableMenuItem<MenuItem>> menuItems = new ArrayList<IdentifiableMenuItem<MenuItem>>(1);
			menuItems.add(new NotificationsMenuItem(activity));
			menuItems.add(new UnreadMessagesCounterMenuItem());
			menuItems.add(new SettingsMenuItem());
			menuItems.add(new AccountsMenuItem());
			menuItems.add(new MenuItemAppExitMenuItem());

			this.menu = ListActivityMenu.fromResource(R.menu.mpp_menu_main, menuItems, SherlockMenuHelper.getInstance());
		}
		return this.menu.onCreateOptionsMenu(activity, menu);
	}

	@Override
	public boolean onOptionsItemSelected(@Nonnull Activity activity, @Nonnull MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onHomeClickListener.run();
				return true;
			default:
				return this.menu.onOptionsItemSelected(activity, item);
		}
	}

    /*
	**********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

	private static final class MenuItemAppExitMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_app_exit;
		}

		@Override
		public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
			App.getEventManager(context).fire(UiEventType.exit);
		}
	}

	private static final class UnreadMessagesCounterMenuItem implements IdentifiableMenuItem<MenuItem> {

		private UnreadMessagesCounterMenuItem() {
		}

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_unread_messages_counter;
		}

		@Override
		public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
			Chats.openUnreadChat(context);
		}
	}

	private static final class NotificationsMenuItem implements IdentifiableMenuItem<MenuItem>, PopupWindow.OnDismissListener {

		@Nullable
		private APopupWindow notificationPopupWindow;

		@Nonnull
		private final Activity activity;

		private NotificationsMenuItem(@Nonnull Activity activity) {
			this.activity = activity;
		}

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_notifications;
		}

		@Override
		public void onClick(@Nonnull final MenuItem menuItem, @Nonnull Context context) {
			if (notificationPopupWindow == null) {
				final List<Notification> notifications = App.getNotificationService().getNotifications();
				if (!notifications.isEmpty()) {
					final NotificationsViewBuilder viewBuilder = new NotificationsViewBuilder(notifications);

					final View menuItemView = activity.findViewById(menuItem.getItemId());
					if (menuItemView == null) {
						final AbsoluteAPopupWindow notificationPopupWindow = new AbsoluteAPopupWindow(activity, viewBuilder);
						notificationPopupWindow.showLikePopDownMenu(activity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
						this.notificationPopupWindow = notificationPopupWindow;
					} else {
						final AnchorAPopupWindow notificationPopupWindow = new AnchorAPopupWindow(menuItemView, viewBuilder);
						final int popupWidth = activity.getResources().getDimensionPixelSize(R.dimen.mpp_popup_notification_width);
						final int popupXOffset = -popupWidth / 2 + menuItemView.getWidth() / 2;
						notificationPopupWindow.showLikePopDownMenu(popupXOffset, 0);
						this.notificationPopupWindow = notificationPopupWindow;
					}
					this.notificationPopupWindow.setOnDismissListener(this);
				}
			} else {
				dismissPopup();
			}
		}

		private void dismissPopup() {
			if (notificationPopupWindow != null) {
				notificationPopupWindow.dismiss();
				notificationPopupWindow = null;
			}
		}

		public void onNotificationsChanged() {
			dismissPopup();
		}

		@Override
		public void onDismiss() {
			notificationPopupWindow = null;
		}
	}


	private static final class SettingsMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_settings;
		}

		@Override
		public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
			getEventManager(context).fire(UiEventType.show_settings.newEvent());
		}
	}

	private static final class AccountsMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_accounts;
		}

		@Override
		public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
			getEventManager(context).fire(UiEventType.show_accounts.newEvent());
		}
	}
}
