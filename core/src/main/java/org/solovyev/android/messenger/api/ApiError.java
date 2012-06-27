package org.solovyev.android.messenger.api;

import android.os.Parcelable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.Captcha;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 11:48 PM
 */
public interface ApiError extends Parcelable {

    @NotNull
    public static final Parcelable.Creator<ApiError> CREATOR = new ApiErrorParcelableCreator();

    @NotNull
    String getErrorId();

    @Nullable
    String getErrorDescription();

    @Nullable
    Captcha getCaptcha();
}
