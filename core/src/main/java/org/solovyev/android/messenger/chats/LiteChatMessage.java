package org.solovyev.android.messenger.chats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.VersionedEntity;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 1:58 PM
 */
public interface LiteChatMessage extends VersionedEntity<String> {

    @NotNull
    User getAuthor();

    @Nullable
    User getRecipient();

    boolean isPrivate();

    @Nullable
    User getSecondUser(@NotNull User user);

    @NotNull
    DateTime getSendDate();

    @NotNull
    String getTitle();

    @NotNull
    String getBody();

    @NotNull
    LiteChatMessage clone();
}
