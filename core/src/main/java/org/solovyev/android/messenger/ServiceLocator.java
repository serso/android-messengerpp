package org.solovyev.android.messenger;

import org.jetbrains.annotations.NotNull;
import org.solovyev.android.http.RemoteFileService;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.registration.RegistrationService;
import org.solovyev.android.messenger.security.AuthService;
import org.solovyev.android.messenger.security.AuthServiceFacade;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.UserService;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:32 PM
 */
public interface ServiceLocator {

    /*
    **********************************************************************
    *
    *                           SERVICES
    *
    **********************************************************************
    */
    @NotNull
    AuthService getAuthService();

    @NotNull
    AuthServiceFacade getAuthServiceFacade();

    @NotNull
    RealmService getRealmService();

    @NotNull
    UserService getUserService();

    @NotNull
    ChatService getChatService();

    @NotNull
    ChatMessageService getChatMessageService();

    @NotNull
    SyncService getSyncService();

    @NotNull
    RemoteFileService getRemoteFileService();

    @NotNull
    RegistrationService getRegistrationService();
}
