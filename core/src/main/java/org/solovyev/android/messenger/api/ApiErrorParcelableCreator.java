package org.solovyev.android.messenger.api;

import android.os.Parcel;
import android.os.Parcelable;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/29/12
 * Time: 9:19 PM
 */
public class ApiErrorParcelableCreator implements Parcelable.Creator<ApiError> {

	@Override
	public ApiError createFromParcel(@Nonnull Parcel in) {
		return CommonApiError.fromParcel(in);
	}

	@Override
	public ApiError[] newArray(int i) {
		return new ApiError[i];
	}
}
