package org.solovyev.android.messenger.messages;

import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.realms.RealmEntityImpl;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/12/13
 * Time: 9:09 PM
 */
public final class ChatMessages {

    private ChatMessages() {
        throw new AssertionError();
    }

    @Nonnull
    public static LiteChatMessage newEmptyMessage(@Nonnull String messageId) {
        return LiteChatMessageImpl.newInstance(RealmEntityImpl.fromEntityId(messageId));
    }

    @Nonnull
    public static LiteChatMessageImpl newMessage(@Nonnull RealmEntity entity) {
        return LiteChatMessageImpl.newInstance(entity);
    }
}
