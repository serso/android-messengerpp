/*
 * Copyright 2014 serso aka se.solovyev
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import org.solovyev.android.Activities;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.isBigScreen;
import static org.solovyev.android.messenger.users.ContactsInfoFragment.newViewContactsFragmentDef;

public class ChatParticipantsActivity extends BaseFragmentActivity {

	private final static String ARGS_CHAT_ID = "chat_id";
	private static final String TAG = App.newTag(ChatParticipantsActivity.class.getSimpleName());

	@Nonnull
	private Entity chatId;

	public static void open(@Nonnull Activity activity, @Nonnull Chat chat) {
		final Intent intent = new Intent(activity, isBigScreen(activity) ? ChatParticipantsActivity.Dialog.class : ChatParticipantsActivity.class);
		intent.putExtra(ARGS_CHAT_ID, chat.getEntity());
		Activities.startActivity(intent, activity);
	}

	public ChatParticipantsActivity() {
		super(false, R.layout.mpp_main_one_pane);
	}

	public ChatParticipantsActivity(boolean dialog, int layoutResId) {
		super(dialog, layoutResId);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();
		final Parcelable chatId = intent.getParcelableExtra(ARGS_CHAT_ID);
		if (chatId instanceof Entity) {
			this.chatId = (Entity) chatId;

			fragmentManager.setMainFragment(newViewContactsFragmentDef(this, getChatService().getParticipants(this.chatId), false));
		} else {
			Log.e(TAG, "Arguments must be provided for " + ChatParticipantsActivity.class);
			finish();
		}
	}

	public static final class Dialog extends ChatParticipantsActivity {
		public Dialog() {
			super(true, R.layout.mpp_dialog);
		}
	}

	@Override
	protected void onChatRemoved(@Nonnull String chatId) {
		super.onChatRemoved(chatId);

		if (this.chatId.getEntityId().equals(chatId)) {
			finish();
		}
	}
}
