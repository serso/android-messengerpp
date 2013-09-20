package org.solovyev.android.messenger.realms.vk.messages;

import android.util.Log;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.chats.MessageDirection;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.messages.ChatMessageImpl;
import org.solovyev.android.messenger.messages.LiteChatMessage;
import org.solovyev.android.messenger.messages.LiteChatMessageImpl;
import org.solovyev.android.messenger.messages.Messages;
import org.solovyev.android.messenger.realms.Account;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 1:07 PM
 */
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
	public LiteChatMessage toLiteChatMessage(@Nonnull User user,
											 @Nullable String explicitUserId,
											 @Nonnull Account account) throws IllegalJsonException {
		if (mid == null || uid == null || date == null) {
			throw new IllegalJsonException();
		}

		final LiteChatMessageImpl result = Messages.newMessage(account.newMessageEntity(mid));

		final MessageDirection messageDirection = getMessageDirection();
		if (messageDirection == MessageDirection.out) {
			result.setAuthor(user.getEntity());
			result.setRecipient(account.newUserEntity(explicitUserId == null ? uid : explicitUserId));
		} else if (messageDirection == MessageDirection.in) {
			result.setAuthor(account.newUserEntity(explicitUserId == null ? uid : explicitUserId));
			result.setRecipient(user.getEntity());
		} else {
			result.setAuthor(account.newUserEntity(uid));
			if (explicitUserId != null) {
				result.setRecipient(account.newUserEntity(explicitUserId));
			}
		}

		DateTime sendDate;
		try {
			sendDate = new DateTime(Long.valueOf(date) * 1000L);
		} catch (NumberFormatException e) {
			Log.e(this.getClass().getSimpleName(), "Date could not be parsed for message: " + mid + ", date: " + date);
			sendDate = DateTime.now();
		}
		result.setSendDate(sendDate);
		result.setBody(Strings.getNotEmpty(body, ""));
		result.setTitle(Strings.getNotEmpty(title, ""));

		return result;
	}

	@Nonnull
	public ChatMessage toChatMessage(@Nonnull User user, @Nullable String explicitUserId, @Nonnull Account account) throws IllegalJsonException {
		if (read_state == null || out == null) {
			throw new IllegalJsonException();
		}

		final ChatMessageImpl result = Messages.newInstance(toLiteChatMessage(user, explicitUserId, account), isRead());
		result.setDirection(getNotNullMessageDirection());
		for (LiteChatMessage fwdMessage : getFwdMessages(user, account)) {
			result.addFwdMessage(fwdMessage);
		}

		return result;
	}

	@Nonnull
	private List<LiteChatMessage> getFwdMessages(@Nonnull User user, @Nonnull Account account) throws IllegalJsonException {
		if (fwd_messages == null) {
			return Collections.emptyList();
		} else {
			final List<LiteChatMessage> result = new ArrayList<LiteChatMessage>(fwd_messages.length);

			for (JsonMessage fwd_message : fwd_messages) {
				// todo serso: think about explicit user id
				result.add(fwd_message.toLiteChatMessage(user, null, account));
			}

			return result;
		}
	}

	@Nonnull
	private MessageDirection getNotNullMessageDirection() {
		if (Integer.valueOf(1).equals(out)) {
			return MessageDirection.out;
		} else {
			return MessageDirection.in;
		}
	}

	@Nullable
	private MessageDirection getMessageDirection() {
		if (Integer.valueOf(1).equals(out)) {
			return MessageDirection.out;
		} else if (Integer.valueOf(0).equals(out)) {
			return MessageDirection.in;
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
