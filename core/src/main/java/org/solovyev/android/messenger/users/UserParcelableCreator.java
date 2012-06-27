package org.solovyev.android.messenger.users;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 9:14 PM
 */
public class UserParcelableCreator implements Parcelable.Creator<User> {

    @Override
    public User createFromParcel(Parcel source) {
        return UserImpl.fromParcel(source);
    }

    @Override
    public User[] newArray(int size) {
        return new User[size];
    }
}
