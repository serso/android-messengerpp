package org.solovyev.android.messenger.messages;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 1:58 PM
 */
public interface LiteChatMessage /*extends MessengerEntity*/ {

    @Nonnull
    Entity getEntity();

    @Nonnull
    User getAuthor();

    @Nullable
    User getRecipient();

    boolean isPrivate();

    @Nullable
    User getSecondUser(@Nonnull User user);

    @Nonnull
    DateTime getSendDate();

    @Nonnull
    String getTitle();

    @Nonnull
    String getBody();

    @Nonnull
    LiteChatMessage clone();
}
