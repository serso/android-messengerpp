package org.solovyev.android.messenger.realms.vk;

import android.content.Context;
import org.solovyev.android.messenger.longpoll.LongPollRealmConnection;
import org.solovyev.android.messenger.realms.vk.longpoll.VkRealmLongPollService;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/6/13
 * Time: 9:21 PM
 */
public class VkLongPollRealmConnection extends LongPollRealmConnection {

	public VkLongPollRealmConnection(@Nonnull VkAccount account, @Nonnull Context context) {
		super(account, context, new VkRealmLongPollService(account));
	}
}
