package org.solovyev.android.messenger.messages;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.realms.RealmEntity;
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
    RealmEntity getEntity();

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
