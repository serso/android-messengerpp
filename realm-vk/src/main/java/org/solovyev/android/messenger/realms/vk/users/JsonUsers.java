package org.solovyev.android.messenger.realms.vk.users;

import java.util.List;

import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 10:16 PM
 */
class JsonUsers {

	@Nullable
	private List<JsonUser> response;

	@Nullable
	public List<JsonUser> getResponse() {
		return response;
	}
}
