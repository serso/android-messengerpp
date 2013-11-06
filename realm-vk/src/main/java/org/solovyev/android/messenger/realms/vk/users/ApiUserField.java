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

package org.solovyev.android.messenger.realms.vk.users;

import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum ApiUserField {
	uid,
	first_name,
	last_name,
	nickname,
	sex,
	online,
	city,
	country,
	timezone,
	photo,
	photo_medium,
	photo_big,
	domain,
	has_mobile,
	rate,
	contacts,
	education,
	bdate;

	@Nullable
	private static String allFieldsRequestParameter;

	@Nonnull
	public static String getAllFieldsRequestParameter() {
		if (allFieldsRequestParameter == null) {
			allFieldsRequestParameter = Strings.getAllEnumValues(ApiUserField.class);
		}

		return allFieldsRequestParameter;
	}
}
