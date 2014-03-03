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

package org.solovyev.android.messenger.chats;

import android.content.Context;
import android.support.v4.app.Fragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.*;
import org.solovyev.android.messenger.accounts.AccountEvent;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.MessagesFragment;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.users.BaseUserFragment;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.common.Builder;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseChatsFragment extends BaseAsyncListFragment<UiChat, ChatListItem> implements DetachableFragment {

	@Nonnull
	protected static final String TAG = "ChatsFragment";

	@Nullable
	private JEventListener<ChatEvent> chatEventListener;

	public BaseChatsFragment() {
		super(TAG, R.string.mpp_chats, true, true);
	}

	@Override
	protected boolean canReuseFragment(@Nonnull Fragment fragment, @Nonnull ChatListItem selectedItem) {
		boolean canReuse = false;
		final Chat chat = selectedItem.getChat();
		if (fragment instanceof MessagesFragment) {
			canReuse = ((MessagesFragment) fragment).getChat().equals(chat);
		} else if (fragment instanceof BaseUserFragment && chat.isPrivate()) {
			final Entity contact = chat.getSecondUser();
			final User fragmentUser = ((BaseUserFragment) fragment).getUser();
			canReuse = fragmentUser != null && fragmentUser.getEntity().equals(contact);
		}
		return canReuse;
	}

	@Override
	protected void attachListeners() {
		super.attachListeners();

		chatEventListener = UiThreadEventListener.onUiThread(this, new ChatEventListener());
		getChatService().addListener(chatEventListener);
	}

	@Override
	protected void detachListeners() {
		if (chatEventListener != null) {
			getChatService().removeListener(chatEventListener);
			chatEventListener = null;
		}

		super.detachListeners();
	}

	private class ChatEventListener extends AbstractJEventListener<ChatEvent> {

		private ChatEventListener() {
			super(ChatEvent.class);
		}

		@Override
		public void onEvent(@Nonnull final ChatEvent event) {
			getAdapter().onEvent(event);
		}
	}

	@Nonnull
	@Override
	protected abstract BaseChatsAdapter createAdapter();

	@Nonnull
	public BaseChatsAdapter getAdapter() {
		return (BaseChatsAdapter) super.getAdapter();
	}

	@Nullable
	@Override
	protected ListViewAwareOnRefreshListener getBottomPullRefreshListener() {
		return null;
	}

	@Nullable
	@Override
	protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
		return new SyncRefreshListener(SyncTask.user_chats);
	}

	@Override
	protected void onEvent(@Nonnull AccountEvent event) {
		super.onEvent(event);
		switch (event.getType()) {
			case state_changed:
				postReload();
				break;
		}
	}

	/*
	**********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

	@Nullable
	@Override
	protected Builder<ActivityMenu<Menu, MenuItem>> newMenuBuilder() {
		return new MenuBuilder();
	}

	private class MenuBuilder implements Builder<ActivityMenu<Menu, MenuItem>> {
		@Nonnull
		@Override
		public ActivityMenu<Menu, MenuItem> build() {
			final List<IdentifiableMenuItem<MenuItem>> menuItems = new ArrayList<IdentifiableMenuItem<MenuItem>>();

			menuItems.add(new ToggleFilterInputMenuItem(BaseChatsFragment.this));
			menuItems.add(new NewChatMenuItem());

			return ListActivityMenu.fromResource(R.menu.mpp_menu_chats, menuItems, SherlockMenuHelper.getInstance());
		}
	}

	private class NewChatMenuItem implements IdentifiableMenuItem<MenuItem> {
		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_new_chat;
		}

		@Override
		public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
			getEventManager().fire(UiEventType.new_chat.newEvent());
		}
	}
}
