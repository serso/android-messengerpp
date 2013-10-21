package org.solovyev.android.messenger.realms.vk;

import android.content.Context;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.longpoll.LongPollAccountConnection;
import org.solovyev.android.messenger.realms.vk.longpoll.VkRealmLongPollService;

public class VkLongPollAccountConnection extends LongPollAccountConnection {

	public VkLongPollAccountConnection(@Nonnull VkAccount account, @Nonnull Context context) {
		super(account, context, new VkRealmLongPollService(account));
	}
}
