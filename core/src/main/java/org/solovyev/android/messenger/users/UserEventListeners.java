package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 1:25 AM
 */
public interface UserEventListeners {

    boolean addListener(@Nonnull UserEventListener listener);

    boolean removeListener(@Nonnull UserEventListener listener);

    void fireUserEvent(@Nonnull User user, @Nonnull UserEventType userEventType, @Nullable Object data);

    void fireUserEvents(@Nonnull List<UserEvent> userEvents);

    public static class UserEvent {

        @Nonnull
        private User user;

        @Nonnull
        private UserEventType userEventType;

        @Nullable
        private Object data;

        public UserEvent(@Nonnull User user, @Nonnull UserEventType userEventType, Object data) {
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
}
