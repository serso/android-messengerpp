package org.solovyev.android.messenger;

import org.solovyev.common.msg.Message;

import javax.annotation.Nonnull;
import java.util.List;

public interface NotificationService {

    void notify(@Nonnull Message notification);

    @Nonnull
    List<Message> getNotifications();

    boolean existNotifications();
}
