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

import com.google.gson.Gson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.common.Objects.areEqual;

public class JsonResult {

	@Nullable
	private String response;


	public static boolean asBoolean(@Nonnull String json) {
		return areEqual(asString(json), "1");
	}

	@Nullable
	public static String asString(@Nonnull String json) {
		final JsonResult result = new Gson().fromJson(json, JsonResult.class);
		return result.response;
	}
}
