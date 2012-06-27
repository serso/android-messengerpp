package org.solovyev.android.messenger.vk.http;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.http.HttpTransaction;
import org.solovyev.android.messenger.api.ApiResponseErrorException;
import org.solovyev.android.messenger.api.CommonApiError;
import org.solovyev.android.messenger.http.IllegalJsonException;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 2:04 PM
 */
public class VkResponseErrorException extends ApiResponseErrorException {

/*    @NotNull
    public static VkResponseErrorException newInstance(@NotNull HttpResponse response) {
        try {
            throw newInstance(StringUtils.convertStream(response.getEntity().getContent()), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

    @NotNull
    public static VkResponseErrorException newInstance(@NotNull String json, @Nullable HttpTransaction<?> httpTransaction) throws IllegalJsonException {
        return new VkResponseErrorException(CommonApiError.fromJson(json), httpTransaction);
    }

    public VkResponseErrorException(@NotNull CommonApiError vkError, @Nullable HttpTransaction<?> httpTransaction) {
        super(vkError, httpTransaction);
    }

    @Override
    @NotNull
    public CommonApiError getApiError() {
        return (CommonApiError)super.getApiError();
    }
}
