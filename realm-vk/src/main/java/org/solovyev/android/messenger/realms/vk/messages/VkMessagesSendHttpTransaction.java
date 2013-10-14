package org.solovyev.android.messenger.realms.vk.messages;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.messages.ChatMessage;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.realms.vk.VkAccount;
import org.solovyev.android.messenger.realms.vk.http.AbstractVkHttpTransaction;
import org.solovyev.common.text.Strings;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

/**
 * User: serso
 * Date: 6/25/12
 * Time: 11:25 PM
 */
public class VkMessagesSendHttpTransaction extends AbstractVkHttpTransaction<String> {

	@Nonnull
	private final ChatMessage chatMessage;

	@Nonnull
	private final Chat chat;

	public VkMessagesSendHttpTransaction(@Nonnull VkAccount realm, @Nonnull ChatMessage chatMessage, @Nonnull Chat chat) {
		super(realm, "messages.send");
		this.chatMessage = chatMessage;
		this.chat = chat;
	}

	@Nonnull
	@Override
	public List<NameValuePair> getRequestParameters() {
		final List<NameValuePair> result = super.getRequestParameters();

		try {

			if (chat.isPrivate()) {
				result.add(new BasicNameValuePair("uid", chat.getSecondUser().getAccountEntityId()));
			}

			if (!chat.isPrivate()) {
				result.add(new BasicNameValuePair("chat_id", chat.getEntity().getAccountEntityId()));
			}

			result.add(new BasicNameValuePair("message", URLEncoder.encode(chatMessage.getBody(), "utf-8")));

			result.add(new BasicNameValuePair("title", URLEncoder.encode(chatMessage.getTitle(), "utf-8")));
			result.add(new BasicNameValuePair("type", chatMessage.isPrivate() ? "0" : "1"));

		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}

		return result;
	}

	@Override
	protected String getResponseFromJson(@Nonnull String json) throws IllegalJsonException {
		return new Gson().fromJson(json, JsonResult.class).response;
	}

	public static class JsonResult {

		@Nullable
		private String response;
	}
}
