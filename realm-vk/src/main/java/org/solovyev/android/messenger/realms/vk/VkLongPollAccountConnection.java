package org.solovyev.android.messenger.realms.vk;

import android.content.Context;
import org.solovyev.android.messenger.longpoll.LongPollAccountConnection;
import org.solovyev.android.messenger.realms.vk.longpoll.VkRealmLongPollService;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/6/13
 * Time: 9:21 PM
 */
public class VkLongPollAccountConnection extends LongPollAccountConnection {

	public VkLongPollAccountConnection(@Nonnull VkAccount account, @Nonnull Context context) {
		super(account, context, new VkRealmLongPollService(account));
	}
}
