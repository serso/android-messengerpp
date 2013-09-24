package org.solovyev.android.messenger.realms.vk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.common.text.Strings;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 8:12 PM
 */
public enum VkAuthScopeParam {
	notify,
	friends,
	messages,
	photos,
	audio,
	video,
	docs,
	notes,
	pages;

	@Nullable
	private static String allFieldsRequestParameter;

	@Nonnull
	public static String getAllFieldsRequestParameter() {
		if (allFieldsRequestParameter == null) {
			allFieldsRequestParameter = Strings.getAllEnumValues(VkAuthScopeParam.class);
		}

		return allFieldsRequestParameter;
	}
}
