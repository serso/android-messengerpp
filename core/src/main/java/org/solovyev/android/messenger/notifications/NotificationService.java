package org.solovyev.android.messenger.notifications;

import org.solovyev.common.msg.Message;
import org.solovyev.common.msg.MessageLevel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface NotificationService {

    void addNotification(int messageResId, @Nonnull MessageLevel level, @Nullable Object... parameters);

    void addNotification(int messageResId, @Nonnull MessageLevel level, @Nonnull List<Object> parameters);

    @Nonnull
    List<Message> getNotifications();

    boolean existNotifications();

    void removeNotification(@Nonnull Message notification);
}
