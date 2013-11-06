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

package org.solovyev.android.messenger.realms.vk.longpoll;

import org.solovyev.android.messenger.http.IllegalJsonException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/23/12
 * Time: 11:59 PM
 */
public class JsonLongPollResponse {

	@Nullable
	private JsonLongPollServerData response;

	@Nullable
	public JsonLongPollServerData getResponse() {
		return response;
	}

	@Nonnull
	public LongPollServerData toLongPollServerData() throws IllegalJsonException {
		if (response == null) {
			throw new IllegalJsonException();
		}

		if (response.getKey() == null || response.getServer() == null || response.getTs() == null) {
			throw new IllegalJsonException();
		}

		return new LongPollServerData(response.getKey(), response.getServer(), response.getTs());
	}
}
