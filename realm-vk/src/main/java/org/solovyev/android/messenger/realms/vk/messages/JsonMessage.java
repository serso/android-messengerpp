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

package org.solovyev.android.messenger.realms.vk.messages;

import android.util.Log;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.chats.MessageDirection;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.chats.MessageDirection.in;
import static org.solovyev.android.messenger.messages.MessageState.*;
import static org.solovyev.android.messenger.messages.Messages.newMessage;
import static org.solovyev.common.text.Strings.getNotEmpty;

public class JsonMessage {

	@Nullable
	private String mid;

	@Nullable
	private String uid;

	@Nullable
	private String date;

	@Nullable
	private Integer read_state;

	@Nullable
	private Integer out;

	@Nullable
	private String title;

	@Nullable
	private String body;

	@Nullable
	private JsonMessageTypedAttachment[] attachments;

	@Nullable
	private JsonMessage[] fwd_messages;

	@Nullable
	private Integer chat_id;

	@Nullable
	private String chat_active;

	@Nullable
	private Integer users_count;

	@Nullable
	private Integer admin_id;

	@Nullable
	public String getMid() {
		return mid;
	}

	@Nullable
	public String getUid() {
		return uid;
	}

	@Nullable
	public String getDate() {
		return date;
	}

	@Nullable
	public Integer getRead_state() {
		return read_state;
	}

	@Nullable
	public Integer getOut() {
		return out;
	}

	@Nullable
	public String getTitle() {
		return title;
	}

	@Nullable
	public String getBody() {
		return body;
	}

	@Nullable
	public JsonMessageTypedAttachment[] getAttachments() {
		return attachments;
	}

	@Nullable
	public JsonMessage[] getFwd_messages() {
		return fwd_messages;
	}

	@Nullable
	public Integer getChat_id() {
		return chat_id;
	}

	@Nullable
	public String getChat_active() {
		return chat_active;
	}

	@Nullable
	public Integer getUsers_count() {
		return users_count;
	}

	@Nullable
	public Integer getAdmin_id() {
		return admin_id;
	}

	@Nonnull
	public MutableMessage toMessage(@Nonnull User user,
									@Nullable String explicitUserId,
									@Nonnull Account account) throws IllegalJsonException {
		if (mid == null || uid == null || date == null || read_state == null || out == null) {
			throw new IllegalJsonException();
		}

		final MutableMessage message = newMessage(account.newMessageEntity(mid));

		final MessageDirection messageDirection = getMessageDirection();
		if (messageDirection != null) {
			switch (messageDirection) {
				case in:
					message.setAuthor(account.newUserEntity(explicitUserId == null ? uid : explicitUserId));
					if (this.chat_id == null) {
						message.setRecipient(user.getEntity());
					}
					break;
				case out:
					message.setAuthor(user.getEntity());
					if (this.chat_id == null) {
						message.setRecipient(account.newUserEntity(explicitUserId == null ? uid : explicitUserId));
					}
					break;
			}
		} else {
			message.setAuthor(account.newUserEntity(uid));
			if (explicitUserId != null && this.chat_id == null) {
				message.setRecipient(account.newUserEntity(explicitUserId));
			}
		}

		if (getNotNullMessageDirection() == in) {
			message.setState(received);
			message.setRead(isRead());
		} else {
			message.setState(isRead() ? delivered : sent);
			message.setRead(true);
		}

		DateTime sendDate;
		try {
			sendDate = new DateTime(Long.valueOf(date) * 1000L);
		} catch (NumberFormatException e) {
			Log.e(this.getClass().getSimpleName(), "Date could not be parsed for message: " + mid + ", date: " + date);
			sendDate = DateTime.now();
		}
		message.setSendDate(sendDate);
		message.setBody(getNotEmpty(body, ""));
		message.setTitle(getNotEmpty(title, ""));

		return message;
	}

	@Nonnull
	private MessageDirection getNotNullMessageDirection() {
		if (Integer.valueOf(1).equals(out)) {
			return MessageDirection.out;
		} else {
			return in;
		}
	}

	@Nullable
	private MessageDirection getMessageDirection() {
		if (Integer.valueOf(1).equals(out)) {
			return MessageDirection.out;
		} else if (Integer.valueOf(0).equals(out)) {
			return in;
		} else {
			return null;
		}
	}

	private boolean isRead() {
		if (Integer.valueOf(1).equals(read_state)) {
			return true;
		} else {
			return false;
		}
	}
}
