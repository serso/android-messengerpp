package org.solovyev.android.messenger.vk.users;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.vk.http.AbstractVkHttpTransaction;
import org.solovyev.common.collections.Collections;
import org.solovyev.common.text.Strings;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 1:43 AM
 */
public class VkUsersGetHttpTransaction extends AbstractVkHttpTransaction<List<User>> {

    @NotNull
    private static final Integer MAX_CHUNK = 1000;

    @NotNull
    private final List<String> userIds;

    @org.jetbrains.annotations.Nullable
    private final List<ApiUserField> apiUserFields;

    private VkUsersGetHttpTransaction(@NotNull Realm realm, @NotNull List<String> userIds, @org.jetbrains.annotations.Nullable List<ApiUserField> apiUserFields) {
        super(realm, "users.get");
        this.apiUserFields = apiUserFields;
        assert !userIds.isEmpty();
        assert userIds.size() <= 1000;
        this.userIds = userIds;
    }

    @NotNull
    public static List<VkUsersGetHttpTransaction> newInstancesForUserIds(@NotNull Realm realm, @NotNull List<String> userIds, @org.jetbrains.annotations.Nullable List<ApiUserField> apiUserFields) {
        final List<VkUsersGetHttpTransaction> result = new ArrayList<VkUsersGetHttpTransaction>();

        for (List<String> userIdsChunk : Collections.split(userIds, MAX_CHUNK)) {
            result.add(new VkUsersGetHttpTransaction(realm, userIdsChunk, apiUserFields));
        }

        return result;
    }

    @NotNull
    public static List<VkUsersGetHttpTransaction> newInstancesForUsers(@NotNull Realm realm, @NotNull List<User> users, @org.jetbrains.annotations.Nullable List<ApiUserField> apiUserFields) {
        return newInstancesForUserIds(realm, Lists.transform(users, new Function<User, String>() {
            @Override
            public String apply(@Nullable User user) {
                assert user != null;
                return user.getRealmUser().getEntityId();
            }
        }), apiUserFields);
    }

    @NotNull
    public static VkUsersGetHttpTransaction newInstance(@NotNull Realm realm, @NotNull String userId, @org.jetbrains.annotations.Nullable List<ApiUserField> apiUserFields) {
        return new VkUsersGetHttpTransaction(realm, Arrays.asList(userId), apiUserFields);
    }

    @NotNull
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
    protected List<User> getResponseFromJson(@NotNull String json) throws IllegalJsonException {
        try {
            return JsonUserConverter.newInstance(getRealm()).convert(json);
        } catch (IllegalJsonRuntimeException e) {
            throw e.getIllegalJsonException();
        }
    }

}
