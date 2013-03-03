package org.solovyev.android.messenger;

import javax.annotation.Nonnull;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.security.AuthService;
import org.solovyev.android.messenger.security.AuthServiceFacade;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.network.NetworkStateService;

/**
 * User: serso
 * Date: 8/13/12
 * Time: 1:40 AM
 */
public interface MessengerServiceLocator {

    @Nonnull
    ChatMessageService getChatMessageService();

    @Nonnull
    UserService getUserService();

    @Nonnull
    ChatService getChatService();

    @Nonnull
    AuthService getAuthService();

    @Nonnull
    SyncService getSyncService();

    @Nonnull
    RealmService getRealmService();

    @Nonnull
    AuthServiceFacade getAuthServiceFacade();

    @Nonnull
    NetworkStateService getNetworkStateService();
}
