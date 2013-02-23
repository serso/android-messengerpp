package org.solovyev.android.messenger.vk.http;

import android.util.Log;
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
            throw newInstance(Strings.convertStream(response.getEntity().getContent()), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

    @NotNull
    public static VkResponseErrorException newInstance(@NotNull String json, @Nullable HttpTransaction<?> httpTransaction) {
        VkResponseErrorException result;
        try {
            result = new VkResponseErrorException(CommonApiError.fromJson(json), httpTransaction);
        } catch (IllegalJsonException e) {
            result = handleException(json, httpTransaction, e);
        } catch (RuntimeException e) {
            result = handleException(json, httpTransaction, e);
        }
        return result;
    }

    @NotNull
    private static VkResponseErrorException handleException(@NotNull String json,
                                                            @NotNull HttpTransaction<?> httpTransaction,
                                                            @NotNull Exception e) {
        VkResponseErrorException result;
        Log.e(VkResponseErrorException.class.getSimpleName(), json);
        Log.e(VkResponseErrorException.class.getSimpleName(), e.getMessage());
        result = new VkResponseErrorException(CommonApiError.newInstance("UnableToParseJson", "Unable to parse JSON from server!"), httpTransaction);
        return result;
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
