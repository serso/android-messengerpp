package org.solovyev.android.messenger.api;

import org.solovyev.android.http.HttpTransaction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 11:48 PM
 */
public class ApiResponseErrorException extends RuntimeException {

	@Nonnull
	private final ApiError apiError;

	@Nullable
	private final HttpTransaction<?> httpTransaction;

	public ApiResponseErrorException(@Nonnull ApiError apiError, @Nullable HttpTransaction<?> httpTransaction) {
		this.apiError = apiError;
		this.httpTransaction = httpTransaction;
	}

	@Nonnull
	public ApiError getApiError() {
		return apiError;
	}
}
