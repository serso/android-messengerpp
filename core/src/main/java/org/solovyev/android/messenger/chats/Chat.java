package org.solovyev.android.messenger.chats;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.properties.AProperty;

import java.util.List;

/**
 * User: serso
 * Date: 6/11/12
 * Time: 7:38 PM
 */
public interface Chat {

    @Nonnull
    RealmEntity getRealmChat();

    boolean isPrivate();

    // must be called only after isPrivate() check
    @Nonnull
    RealmEntity getSecondUser();

    @Nonnull
    Integer getMessagesCount();

    @Nullable
    DateTime getLastMessagesSyncDate();

    @Nonnull
    List<AProperty> getProperties();

    @Nonnull
    Chat updateMessagesSyncDate();
}
