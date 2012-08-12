package org.solovyev.android.messenger;

import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.security.AuthService;
import org.solovyev.android.messenger.security.AuthServiceFacade;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.UserService;

/**
 * User: serso
 * Date: 8/13/12
 * Time: 1:40 AM
 */
public interface MessengerServiceLocator {

    @NotNull
    ChatMessageService getChatMessageService();

    @NotNull
    UserService getUserService();

    @NotNull
    ChatService getChatService();

    @NotNull
    AuthService getAuthService();

    @NotNull
    SyncService getSyncService();

    @NotNull
    RealmService getRealmService();

    @NotNull
    AuthServiceFacade getAuthServiceFacade();
}
