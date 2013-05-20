package org.solovyev.android.messenger.realms.vk.http;

import android.util.Log;
import org.solovyev.android.http.HttpTransaction;
import org.solovyev.android.messenger.api.ApiResponseErrorException;
import org.solovyev.android.messenger.api.CommonApiError;
import org.solovyev.android.messenger.http.IllegalJsonException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 2:04 PM
 */
public class VkResponseErrorException extends ApiResponseErrorException {

/*    @Nonnull
	public static VkResponseErrorException newInstance(@Nonnull HttpResponse response) {
        try {
            throw forClass(Strings.convertStream(response.getEntity().getContent()), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

	@Nonnull
	public static VkResponseErrorException newInstance(@Nonnull String json, @Nullable HttpTransaction<?> httpTransaction) {
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

	@Nonnull
	private static VkResponseErrorException handleException(@Nonnull String json,
															@Nonnull HttpTransaction<?> httpTransaction,
															@Nonnull Exception e) {
		VkResponseErrorException result;
		Log.e(VkResponseErrorException.class.getSimpleName(), json);
		Log.e(VkResponseErrorException.class.getSimpleName(), e.getMessage());
		result = new VkResponseErrorException(CommonApiError.newInstance("UnableToParseJson", "Unable to parse JSON from server!"), httpTransaction);
		return result;
	}

	public VkResponseErrorException(@Nonnull CommonApiError vkError, @Nullable HttpTransaction<?> httpTransaction) {
		super(vkError, httpTransaction);
	}

	@Override
	@Nonnull
	public CommonApiError getApiError() {
		return (CommonApiError) super.getApiError();
	}
}
