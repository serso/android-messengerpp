package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:10 PM
 */
public interface ApiChat {

    @Nonnull
    List<ChatMessage> getMessages();

    @Nullable
    ChatMessage getLastMessage();

    @Nonnull
    List<User> getParticipants();

    @Nonnull
    List<User> getParticipantsExcept(@Nonnull User user);

    @Nonnull
    Chat getChat();

    @Nonnull
    ApiChat copyWithNew(@Nonnull RealmEntity newRealmChat);
}
