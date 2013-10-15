package org.solovyev.android.messenger.realms.vk.chats;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.chats.AccountChat;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.realms.vk.VkAccount;
import org.solovyev.android.messenger.realms.vk.http.AbstractVkHttpTransaction;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 6/10/12
 * Time: 10:05 PM
 */
public class VkMessagesGetHttpTransaction extends AbstractVkHttpTransaction<List<Message>> {

	@Nullable
	private Integer count;

	@Nonnull
	private User user;

	protected VkMessagesGetHttpTransaction(@Nonnull VkAccount realm, @Nonnull User user) {
		super(realm, "messages.get");
		this.user = user;
	}

	@Nonnull
	@Override
	public List<NameValuePair> getRequestParameters() {
		final List<NameValuePair> requestParameters = super.getRequestParameters();

		if (count != null) {
			requestParameters.add(new BasicNameValuePair("count", String.valueOf(count)));
		}

		return requestParameters;
	}

	@Override
	protected List<Message> getResponseFromJson(@Nonnull String json) throws IllegalJsonException {
		final List<AccountChat> chats = new JsonChatConverter(user, null, null, App.getUserService(), getRealm()).convert(json);

		// todo serso: optimize - convert json to the messages directly
		final List<Message> messages = new ArrayList<Message>(chats.size() * 10);
		for (AccountChat chat : chats) {
			messages.addAll(chat.getMessages());
		}

		return messages;
	}
}
