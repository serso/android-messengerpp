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

package org.solovyev.android.messenger.realms.vk;

import android.content.Context;
import org.solovyev.android.messenger.longpoll.LongPollAccountConnection;
import org.solovyev.android.messenger.realms.vk.longpoll.VkRealmLongPollService;

import javax.annotation.Nonnull;

// NOTE: for some reason now VK connections may be lost while using mobile internet (HTTP Error 504: Gateway Timeout). As this error is not on our side and can be solved by reconnection let's set infinite time of reconnection.
public class VkLongPollAccountConnection extends LongPollAccountConnection {

	public VkLongPollAccountConnection(@Nonnull VkAccount account, @Nonnull Context context) {
		super(account, context, new VkRealmLongPollService(account), Integer.MAX_VALUE);
	}
}
