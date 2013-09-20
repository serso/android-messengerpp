package org.solovyev.android.messenger.realms.vk.chats;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.realms.vk.VkAccount;
import org.solovyev.android.messenger.realms.vk.http.AbstractVkHttpTransaction;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 1:03 PM
 */
public class VkMessagesGetDialogsHttpTransaction extends AbstractVkHttpTransaction<List<ApiChat>> {

	@Nonnull
	private static final Integer MAX_COUNT = 100;

	@Nonnull
	private final Integer count;

	@Nonnull
	private final User user;

	private VkMessagesGetDialogsHttpTransaction(@Nonnull VkAccount realm, @Nonnull Integer count, @Nonnull User user) {
		super(realm, "messages.getDialogs");
		this.count = count;
		this.user = user;
	}

	@Nonnull
	public static VkMessagesGetDialogsHttpTransaction newInstance(@Nonnull VkAccount realm, @Nonnull User user) {
		return new VkMessagesGetDialogsHttpTransaction(realm, MAX_COUNT, user);
	}

	@Nonnull
	public static List<VkMessagesGetDialogsHttpTransaction> newInstances(@Nonnull VkAccount realm, @Nonnull Integer count, @Nonnull User user) {
		final List<VkMessagesGetDialogsHttpTransaction> result = new ArrayList<VkMessagesGetDialogsHttpTransaction>();

		for (int i = 0; i < count / MAX_COUNT; i++) {
			result.add(new VkMessagesGetDialogsHttpTransaction(realm, MAX_COUNT, user));
		}

		if (count % MAX_COUNT != 0) {
			result.add(new VkMessagesGetDialogsHttpTransaction(realm, count % MAX_COUNT, user));
		}

		return result;
	}

	@Nonnull
	@Override
	public List<NameValuePair> getRequestParameters() {
		final List<NameValuePair> result = super.getRequestParameters();

		result.add(new BasicNameValuePair("count", String.valueOf(count)));

		return result;
	}

	@Override
	protected List<ApiChat> getResponseFromJson(@Nonnull String json) throws IllegalJsonException {
		return new JsonChatConverter(user, null, null, MessengerApplication.getServiceLocator().getUserService(), getRealm()).convert(json);
	}
}
