package org.solovyev.android.messenger.realms.vk.users;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.realms.vk.VkAccount;
import org.solovyev.android.messenger.realms.vk.http.AbstractVkHttpTransaction;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.collections.Collections;
import org.solovyev.common.text.Strings;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 1:43 AM
 */
public class VkUsersGetHttpTransaction extends AbstractVkHttpTransaction<List<User>> {

	@Nonnull
	private static final Integer MAX_CHUNK = 1000;

	@Nonnull
	private final List<String> userIds;

	@javax.annotation.Nullable
	private final List<ApiUserField> apiUserFields;

	private VkUsersGetHttpTransaction(@Nonnull VkAccount realm, @Nonnull List<String> userIds, @javax.annotation.Nullable List<ApiUserField> apiUserFields) {
		super(realm, "users.get");
		this.apiUserFields = apiUserFields;
		assert !userIds.isEmpty();
		assert userIds.size() <= 1000;
		this.userIds = userIds;
	}

	@Nonnull
	public static List<VkUsersGetHttpTransaction> newInstancesForUserIds(@Nonnull VkAccount realm, @Nonnull List<String> userIds, @javax.annotation.Nullable List<ApiUserField> apiUserFields) {
		final List<VkUsersGetHttpTransaction> result = new ArrayList<VkUsersGetHttpTransaction>();

		for (List<String> userIdsChunk : Collections.split(userIds, MAX_CHUNK)) {
			result.add(new VkUsersGetHttpTransaction(realm, userIdsChunk, apiUserFields));
		}

		return result;
	}

	@Nonnull
	public static List<VkUsersGetHttpTransaction> newInstancesForUsers(@Nonnull VkAccount realm, @Nonnull List<User> users, @javax.annotation.Nullable List<ApiUserField> apiUserFields) {
		return newInstancesForUserIds(realm, Lists.transform(users, new Function<User, String>() {
			@Override
			public String apply(@Nullable User user) {
				assert user != null;
				return user.getEntity().getEntityId();
			}
		}), apiUserFields);
	}

	@Nonnull
	public static VkUsersGetHttpTransaction newInstance(@Nonnull VkAccount realm, @Nonnull String userId, @javax.annotation.Nullable List<ApiUserField> apiUserFields) {
		return new VkUsersGetHttpTransaction(realm, Arrays.asList(userId), apiUserFields);
	}

	@Nonnull
	@Override
	public List<NameValuePair> getRequestParameters() {
		final List<NameValuePair> result = new ArrayList<NameValuePair>();

		result.add(new BasicNameValuePair("uids", Strings.getAllValues(userIds)));
		if (Collections.isEmpty(apiUserFields)) {
			result.add(new BasicNameValuePair("fields", ApiUserField.getAllFieldsRequestParameter()));
		} else {
			result.add(new BasicNameValuePair("fields", Strings.getAllValues(apiUserFields)));
		}

		return result;
	}

	@Override
	protected List<User> getResponseFromJson(@Nonnull String json) throws IllegalJsonException {
		try {
			return JsonUserConverter.newInstance(getRealm()).convert(json);
		} catch (IllegalJsonRuntimeException e) {
			throw e.getIllegalJsonException();
		}
	}

}
