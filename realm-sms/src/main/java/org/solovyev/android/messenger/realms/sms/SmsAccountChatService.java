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

package org.solovyev.android.messenger.realms.sms;

import android.app.Application;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.getBroadcast;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.solovyev.android.messenger.App.getApplication;
import static org.solovyev.android.messenger.App.getMessageService;
import static org.solovyev.android.messenger.chats.Chats.newAccountChat;
import static org.solovyev.android.messenger.realms.sms.SmsAccount.TAG;
import static org.solovyev.android.messenger.realms.sms.SmsRealm.*;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;

final class SmsAccountChatService implements AccountChatService {

	@Nonnull
	static final String MESSAGE_PROPERTY_PHONE = "phone_number";

	@Nonnull
	private static final Uri SMS_URI = Uri.parse("content://sms");

	@Nonnull
	private final SmsAccount account;

	public SmsAccountChatService(@Nonnull SmsAccount account) {
		this.account = account;
	}

	@Nonnull
	@Override
	public List<? extends Message> getMessages() throws AccountConnectionException {
		return readMessages();
	}

	@Nonnull
	@Override
	public List<Message> getNewerMessagesForChat(@Nonnull String accountChatId) throws AccountConnectionException {
		return emptyList();
	}

	@Nonnull
	@Override
	public List<Message> getOlderMessagesForChat(@Nonnull String accountChatId, @Nonnull Integer offset) throws AccountConnectionException {
		return emptyList();
	}

	@Nonnull
	@Override
	public List<AccountChat> getChats() throws AccountConnectionException {
		final Map<Entity, AccountChat> chats = new HashMap<Entity, AccountChat>();

		final List<MutableMessage> messages = readMessages();
		for (MutableMessage message : messages) {
			final Entity chatId = message.getChat();
			MutableAccountChat chat = (MutableAccountChat) chats.get(chatId);
			if (chat == null) {
				chat = newAccountChat(chatId, true);
				chats.put(chatId, chat);
			}
			chat.addMessage(message);
			chat.addParticipant(newEmptyUser(message.getAuthor()));
			chat.addParticipant(newEmptyUser(message.getRecipient()));
		}

		return newArrayList(chats.values());
	}

	@Nonnull
	private List<MutableMessage> readMessages() {
		Log.d(TAG, "Reading messages...");
		final List<MutableMessage> messages = new ArrayList<MutableMessage>();

		final SmsMessageConverter converter = new SmsMessageConverter(account, getMessageService());

		Cursor cursor = null;
		try {
			cursor = getApplication().getContentResolver().query(SMS_URI, null, null, null, null);
			if (cursor == null) {
				Log.w(TAG, "Unable to read chats - cursor is null");
			} else {
				boolean hasCurrent = cursor.moveToFirst();
				while (hasCurrent) {
					try {
						messages.add(converter.convert(cursor));
					} catch (IllegalArgumentException e) {
						// do nothing, we ourselves throw it
					} catch (Throwable e) {
						Log.e(TAG, e.getMessage(), e);
					}
					hasCurrent = cursor.moveToNext();
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		Log.d(TAG, "Messages read");

		return messages;
	}

	@Nullable
	@Override
	public String sendMessage(@Nonnull Chat chat, @Nonnull Message message) throws AccountConnectionException {
		final String phoneNumber = getPhoneNumber(message);
		if (!Strings.isEmpty(phoneNumber)) {
			final Application application = getApplication();

			final Intent sentIntent = new Intent(makeSmsSentAction(message.getId()));
			sentIntent.putExtra(INTENT_EXTRA_SMS_ID, message.getEntity().getEntityId());

			final Intent deliveredIntent = new Intent(makeSmsDeliveredAction(message.getId()));
			deliveredIntent.putExtra(INTENT_EXTRA_SMS_ID, message.getEntity().getEntityId());

			final SmsAccountConnection connection = account.getAccountConnection();
			if (connection != null) {
				final BroadcastReceiver receiver = connection.getReceiver();
				application.registerReceiver(receiver, new IntentFilter(sentIntent.getAction()));
				application.registerReceiver(receiver, new IntentFilter(deliveredIntent.getAction()));
			}

			SmsManager.getDefault().sendTextMessage(phoneNumber, null, message.getBody(),
					getBroadcast(application, 0, sentIntent, FLAG_ONE_SHOT),
					getBroadcast(application, 0, deliveredIntent, FLAG_ONE_SHOT));

			return null;
		}
		return null;
	}

	@Nullable
	private String getPhoneNumber(@Nonnull Message message) {
		String phoneNumber = message.getProperties().getPropertyValue(MESSAGE_PROPERTY_PHONE);
		if (Strings.isEmpty(phoneNumber)) {
			final User recipient = App.getUserService().getUserById(message.getRecipient());
			phoneNumber = recipient.getPropertyValueByName(User.PROPERTY_PHONE);
		}
		return phoneNumber;
	}

	@Override
	public void beforeSendMessage(@Nonnull Chat chat, @Nullable User recipient, @Nonnull MutableMessage message) throws AccountConnectionException {
		if (recipient == null) {
			recipient = App.getUserService().getUserById(message.getRecipient());
		}

		final String phoneNumber = recipient.getPhoneNumber();
		if (phoneNumber != null) {
			message.getProperties().setProperty(MESSAGE_PROPERTY_PHONE, phoneNumber);
		}
	}

	@Nonnull
	@Override
	public MutableChat newPrivateChat(@Nonnull Entity accountChat, @Nonnull String accountUserId1, @Nonnull String accountUserId2) throws AccountConnectionException {
		return Chats.newPrivateChat(accountChat);
	}

	@Override
	public boolean markMessageRead(@Nonnull Message message) throws AccountConnectionException {
		final ContentResolver cr = getApplication().getContentResolver();

		try {
			final int messageId = findMessageId(message, cr);
			if (messageId >= 0) {
				final ContentValues values = new ContentValues();
				values.put("read", true);
				cr.update(SMS_URI, values, "_id=" + messageId, null);
			}
		} catch (Exception e) {
			// OK, we can't update SMS in the inbox
			Log.e(TAG, e.getMessage(), e);
		}

		// we always want to update read marker in local storage => always return true
		return true;
	}

	private int findMessageId(@Nonnull Message message, @Nonnull ContentResolver cr) {
		int messageId = -1;

		final Cursor cursor = cr.query(SMS_URI, new String[]{"_id", "address", "body", "date_sent"}, "address = ? and date_sent = ?", new String[]{message.getAuthor().getAccountEntityId(), String.valueOf(message.getSendDate().getMillis())}, null);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					messageId = cursor.getInt(0);
				}
			} finally {
				cursor.close();
			}
		}

		return messageId;
	}
}
