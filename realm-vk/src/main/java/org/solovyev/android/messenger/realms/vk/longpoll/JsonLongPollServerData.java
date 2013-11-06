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

import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/24/12
 * Time: 12:00 AM
 */
public class JsonLongPollServerData {

	@Nullable
	private String key;

	@Nullable
	private String server;

	@Nullable
	private Long ts;

	@Nullable
	public String getKey() {
		return key;
	}

	@Nullable
	public String getServer() {
		return server;
	}

	@Nullable
	public Long getTs() {
		return ts;
	}
}
