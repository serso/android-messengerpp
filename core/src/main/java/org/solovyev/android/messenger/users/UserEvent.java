package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.events.AbstractTypedJEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
* User: serso
* Date: 3/9/13
* Time: 2:44 PM
*/
class UserEvent extends AbstractTypedJEvent<User, UserEventType> {

    UserEvent(@Nonnull User user, @Nonnull UserEventType type, @Nullable Object data) {
        super(user, type, data);
    }

    @Nonnull
    public User getUser() {
        return getEventObject();
    }
}
