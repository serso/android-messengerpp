package org.solovyev.android.messenger;

import org.solovyev.android.messenger.api.ApiError;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 9:58 PM
 */
interface ServiceCallback {

    void onSuccess();

    void onFailure(String e);

    void onApiError(in ApiError apiError);
}
