package org.solovyev.android.messenger.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.http.HttpTransaction;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 11:48 PM
 */
public class ApiResponseErrorException extends RuntimeException {

    @NotNull
    private final ApiError apiError;

    @Nullable
    private final HttpTransaction<?> httpTransaction;

    public ApiResponseErrorException(@NotNull ApiError apiError, @Nullable HttpTransaction<?> httpTransaction) {
        this.apiError = apiError;
        this.httpTransaction = httpTransaction;
    }

    @NotNull
    public ApiError getApiError() {
        return apiError;
    }
}
