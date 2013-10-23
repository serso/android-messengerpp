package org.solovyev.android.messenger.realms.vk;

import android.content.Context;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.longpoll.LongPollAccountConnection;
import org.solovyev.android.messenger.realms.vk.longpoll.VkRealmLongPollService;

// NOTE: for some reason now VK connections may be lost while using mobile internet (HTTP Error 504: Gateway Timeout). As this error is not on our side and can be solved by reconnection let's set infinite time of reconnection.
public class VkLongPollAccountConnection extends LongPollAccountConnection {

	public VkLongPollAccountConnection(@Nonnull VkAccount account, @Nonnull Context context) {
		super(account, context, new VkRealmLongPollService(account), Integer.MAX_VALUE);
	}
}
