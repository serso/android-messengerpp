package org.solovyev.android.messenger.chats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.VersionedEntity;

import java.util.List;

/**
 * User: serso
 * Date: 6/11/12
 * Time: 7:38 PM
 */
public interface Chat extends VersionedEntity<String> {

    @NotNull
    RealmEntity getRealmChat();

    boolean isPrivate();

    // must be called only after isPrivate() check
    @NotNull
    String getSecondUserId();

    @NotNull
    Integer getMessagesCount();

    @Nullable
    DateTime getLastMessagesSyncDate();

    @NotNull
    List<AProperty> getProperties();

    @NotNull
    Chat updateMessagesSyncDate();
}
