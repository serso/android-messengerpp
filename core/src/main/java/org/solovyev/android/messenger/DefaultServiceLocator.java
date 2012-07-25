package org.solovyev.android.messenger;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.http.HttpRemoteFileService;
import org.solovyev.android.http.RemoteFileService;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.DefaultChatService;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.messages.DefaultChatMessageService;
import org.solovyev.android.messenger.realms.DefaultRealmService;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.registration.RegistrationService;
import org.solovyev.android.messenger.security.AuthService;
import org.solovyev.android.messenger.security.AuthServiceFacade;
import org.solovyev.android.messenger.security.AuthServiceFacadeImpl;
import org.solovyev.android.messenger.security.AuthServiceImpl;
import org.solovyev.android.messenger.sync.DefaultSyncService;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.DefaultUserService;
import org.solovyev.android.messenger.users.UserService;

import java.util.Arrays;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 10:27 PM
 */
public class DefaultServiceLocator implements ServiceLocator {

    private AuthService authService;

    private AuthServiceFacade authServiceFacade;

    @NotNull
    private final DefaultUserService userService;

    @NotNull
    private final DefaultChatService chatService;

    @NotNull
    private final RealmService realmService;

    @NotNull
    private final DefaultChatMessageService chatMessageService = new DefaultChatMessageService();

    private SyncService syncService;

    @NotNull
    private RemoteFileService remoteFileService;

    @NotNull
    private RegistrationService registrationService;

    public DefaultServiceLocator(@NotNull Context context,
                                 @NotNull RegistrationService registrationService,
                                 @NotNull Realm realm) {
        this.registrationService = registrationService;
        this.remoteFileService = new HttpRemoteFileService(context, "messenger");
        this.realmService = new DefaultRealmService(Arrays.asList(realm));

        userService = new DefaultUserService(realm);
        chatService = new DefaultChatService(realm);
        userService.addUserEventListener(chatService);
        chatService.addChatEventListener(userService);
    }

    @NotNull
    @Override
    public synchronized AuthService getAuthService() {
        if (authService == null) {
            authService = new AuthServiceImpl();
        }
        return authService;
    }

    @NotNull
    @Override
    public AuthServiceFacade getAuthServiceFacade() {
        if (authServiceFacade == null) {
            authServiceFacade = new AuthServiceFacadeImpl(MessengerConfigurationImpl.getInstance().getRealm().getId(), getAuthService());
        }
        return authServiceFacade;
    }

    @NotNull
    @Override
    public RealmService getRealmService() {
        return this.realmService;
    }

    @NotNull
    @Override
    public synchronized UserService getUserService() {
        return userService;
    }

    @NotNull
    @Override
    public synchronized ChatService getChatService() {
        return chatService;
    }

    @NotNull
    @Override
    public ChatMessageService getChatMessageService() {
        return chatMessageService;
    }

    @NotNull
    @Override
    public synchronized SyncService getSyncService() {
        if (syncService == null) {
            syncService = new DefaultSyncService();
        }
        return syncService;
    }

    @NotNull
    @Override
    public synchronized RemoteFileService getRemoteFileService() {
        return remoteFileService;
    }


    @NotNull
    @Override
    public RegistrationService getRegistrationService() {
        return registrationService;
    }
}
