package org.solovyev.android.messenger.realms.vk.users;

import com.google.common.base.Function;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.Converter;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.solovyev.android.messenger.realms.vk.users.JsonUsers.newFromJson;

public class JsonUserConverter implements Converter<String, List<User>> {

	@Nonnull
	private final Account account;

	private JsonUserConverter(@Nonnull Account account) {
		this.account = account;
	}

	@Nonnull
	@Override
	public List<User> convert(@Nonnull String json) {
		final JsonUsers jsonUsersResult = newFromJson(json);
		final List<JsonUser> jsonUsers = jsonUsersResult.getUsers();
		return newArrayList(transform(jsonUsers, new Function<JsonUser, User>() {
			@Override
			public User apply(JsonUser jsonUser) {
				try {
					return jsonUser.toUser(account);
				} catch (IllegalJsonException e) {
					throw new IllegalJsonRuntimeException(e);
				}
			}
		}));
	}

	@Nonnull
	public static Converter<String, List<User>> newInstance(@Nonnull Account account) {
		return new JsonUserConverter(account);
	}
}
