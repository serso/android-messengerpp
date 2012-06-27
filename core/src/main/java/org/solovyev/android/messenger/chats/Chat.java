package org.solovyev.android.messenger.chats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.solovyev.android.AProperty;

import java.util.List;

/**
 * User: serso
 * Date: 6/11/12
 * Time: 7:38 PM
 */
public interface Chat {

    @NotNull
    String getId();

    boolean isPrivate();

    // must be called only after isPrivate() check
    @NotNull
    Integer getSecondUserId();

    @NotNull
    Integer getMessagesCount();

    @Nullable
    DateTime getLastMessagesSyncDate();

    @NotNull
    List<AProperty> getProperties();

    @NotNull
    Chat updateMessagesSyncDate();
}
