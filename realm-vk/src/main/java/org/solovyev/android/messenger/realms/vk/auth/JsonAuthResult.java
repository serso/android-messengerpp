package org.solovyev.android.messenger.realms.vk.auth;

import com.google.gson.Gson;
import org.solovyev.android.messenger.http.IllegalJsonException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 11:37 PM
 */
public final class JsonAuthResult {

	@Nullable
	private String access_token;

	@Nullable
	private Integer expires_in;

	@Nullable
	private String user_id;

	@Nonnull
	public static JsonAuthResult fromJson(@Nonnull String json) throws IllegalJsonException {
		final Gson gson = new Gson();

		final JsonAuthResult result = gson.fromJson(json, JsonAuthResult.class);

		if (result.access_token == null || result.expires_in == null || result.user_id == null) {
			throw new IllegalJsonException();
		}

		return result;
	}

	@Nullable
	public String getAccessToken() {
		return access_token;
	}

	@Nullable
	public Integer getExpiresIn() {
		return expires_in;
	}

	@Nullable
	public String getUserId() {
		return user_id;
	}
}
