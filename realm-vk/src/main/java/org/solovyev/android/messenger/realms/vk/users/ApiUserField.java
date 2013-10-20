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
