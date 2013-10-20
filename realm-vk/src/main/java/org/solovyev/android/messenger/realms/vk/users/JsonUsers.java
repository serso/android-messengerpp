package org.solovyev.android.messenger.realms.vk.users;

import com.google.gson.Gson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

class JsonUsers {

	@Nullable
	private List<JsonUser> response;

	@Nonnull
	public List<JsonUser> getUsers() {
		return response == null ? Collections.<JsonUser>emptyList() : response;
	}

	@Nonnull
	static JsonUsers newFromJson(@Nonnull String json) {
		final Gson gson = new Gson();
		return gson.fromJson(json, JsonUsers.class);
	}
}
