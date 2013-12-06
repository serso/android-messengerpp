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

package org.solovyev.android.messenger.messages;

import android.content.Context;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.view.PullToRefreshListViewProvider;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import static org.solovyev.android.messenger.App.getChatService;

public class SyncMessagesForChatAsyncTask extends MessengerAsyncTask<SyncMessagesForChatAsyncTask.Input, Void, SyncMessagesForChatAsyncTask.Input> {

	@Nullable
	private PullToRefreshListViewProvider listViewProvider;

	public SyncMessagesForChatAsyncTask(@Nullable PullToRefreshListViewProvider listViewProvider,
										@Nonnull Context context) {
		super(context);
		this.listViewProvider = listViewProvider;
	}

	@Override
	protected Input doWork(@Nonnull List<Input> inputs) {
		assert inputs.size() == 1;
		final Input input = inputs.get(0);

		final Context context = getContext();
		if (context != null) {
			try {
				if (!input.loadOlder) {
					getChatService().syncNewerMessagesForChat(input.chat);
				} else {
					getChatService().syncOlderMessagesForChat(input.chat, input.user);
				}
			} catch (AccountException e) {
				throwException(e);
			}

		}

		return input;
	}

	@Override
	protected void onSuccessPostExecute(@Nonnull Input result) {
		completeRefreshForListView();
	}

	@Override
	protected void onFailurePostExecute(@Nonnull Exception e) {
		completeRefreshForListView();
		super.onFailurePostExecute(e);
	}

	private void completeRefreshForListView() {
		if (listViewProvider != null) {
			final PullToRefreshListView ptrlv = listViewProvider.getPullToRefreshListView();
			if (ptrlv != null) {
				ptrlv.onRefreshComplete();
			}
		}
	}

	public static class Input {

		@Nonnull
		private Entity user;

		@Nonnull
		private Entity chat;

		private boolean loadOlder;

		public Input(@Nonnull Entity user,
					 @Nonnull Entity chat,
					 boolean loadOlder) {
			this.user = user;
			this.chat = chat;
			this.loadOlder = loadOlder;
		}
	}
}
