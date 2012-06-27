package org.solovyev.android.messenger.vk.secutiry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.common.utils.StringUtils2;

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

    @NotNull
    public static String getAllFieldsRequestParameter() {
        if (allFieldsRequestParameter == null) {
            allFieldsRequestParameter = StringUtils2.getAllEnumValues(VkAuthScopeParam.class);
        }

        return allFieldsRequestParameter;
    }
}
