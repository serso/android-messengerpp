package org.solovyev.android.messenger.api;

import android.os.Parcel;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.captcha.Captcha;
import org.solovyev.android.messenger.http.IllegalJsonException;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 2:05 PM
 */
public class CommonApiError implements ApiError {

    @NotNull
    private String errorId;

    @Nullable
    private String errorDescription;

    @Nullable
    private Captcha captcha;

    @NotNull
    public static CommonApiError newInstance(@NotNull String errorId, @Nullable String errorDescription) {
        final CommonApiError result = new CommonApiError();

        result.errorId = errorId;
        result.errorDescription = errorDescription;

        return result;
    }

    @NotNull
    public static CommonApiError fromJson(@NotNull String json) throws IllegalJsonException {
        final Gson gson = new Gson();
        final CommonErrorJson commonJsonError = gson.fromJson(json, CommonErrorJson.class);
        if (commonJsonError.error == null) {
            throw new IllegalJsonException();
        }
        return fromJson(commonJsonError);
    }

    @NotNull
    private static CommonApiError fromJson(@NotNull CommonErrorJson json) throws IllegalJsonException {
        final CommonApiError result = new CommonApiError();

        result.errorId = json.error;
        result.errorDescription = json.error_description;
        if (json.captcha_sid != null) {
            if (json.captcha_img == null) {
                throw new IllegalJsonException();
            }
            result.captcha = new Captcha(json.captcha_sid, json.captcha_img);
        }

        return result;
    }

    @NotNull
    public static CommonApiError fromParcel(@NotNull Parcel in) {
        final CommonApiError result = new CommonApiError();

        result.errorId = in.readString();
        result.errorDescription = in.readString();
        result.captcha = in.readParcelable(Thread.currentThread().getContextClassLoader());

        return result;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(errorId);
        out.writeString(errorDescription);
        out.writeParcelable(captcha, i);
    }

    private static class CommonErrorJson {

        @Nullable
        private String error;

        @Nullable
        private String error_description;

        @Nullable
        private String captcha_sid;

        @Nullable
        private String captcha_img;

    }

    @Override
    @NotNull
    public String getErrorId() {
        return errorId;
    }

    @Override
    @Nullable
    public String getErrorDescription() {
        return errorDescription;
    }

    @Override
    @Nullable
    public Captcha getCaptcha() {
        return captcha;
    }
}
