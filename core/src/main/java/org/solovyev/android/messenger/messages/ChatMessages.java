package org.solovyev.android.messenger.messages;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityImpl;

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
        return LiteChatMessageImpl.newInstance(EntityImpl.fromEntityId(messageId));
    }

    @Nonnull
    public static LiteChatMessageImpl newMessage(@Nonnull Entity entity) {
        return LiteChatMessageImpl.newInstance(entity);
    }
}
