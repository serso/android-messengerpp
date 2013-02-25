package org.solovyev.android.messenger.vk.users;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.common.text.Strings;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 10:09 PM
 */
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

    @NotNull
    public static String getAllFieldsRequestParameter() {
        if (allFieldsRequestParameter == null) {
            allFieldsRequestParameter = Strings.getAllEnumValues(ApiUserField.class);
        }

        return allFieldsRequestParameter;
    }
}
