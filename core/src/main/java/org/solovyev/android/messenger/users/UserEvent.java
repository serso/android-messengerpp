package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
* User: serso
* Date: 3/9/13
* Time: 2:44 PM
*/
class UserEvent {

    @Nonnull
    private User user;

    @Nonnull
    private UserEventType userEventType;

    @Nullable
    private Object data;

    UserEvent(@Nonnull User user, @Nonnull UserEventType userEventType, @Nullable Object data) {
        this.user = user;
        this.userEventType = userEventType;
        this.data = data;
    }

    @Nonnull
    public User getUser() {
        return user;
    }

    @Nonnull
    public UserEventType getUserEventType() {
        return userEventType;
    }

    @Nullable
    public Object getData() {
        return data;
    }
}
