package org.solovyev.android.messenger;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.http.HttpRemoteFileService;
import org.solovyev.android.http.RemoteFileService;
import org.solovyev.android.messenger.chats.ApiChatService;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.DefaultChatService;
import org.solovyev.android.messenger.longpoll.ApiLongPollService;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.messages.DefaultChatMessageService;
import org.solovyev.android.messenger.registration.RegistrationService;
import org.solovyev.android.messenger.security.AuthService;
import org.solovyev.android.messenger.security.AuthServiceFacade;
import org.solovyev.android.messenger.security.AuthServiceFacadeImpl;
import org.solovyev.android.messenger.security.AuthServiceImpl;
import org.solovyev.android.messenger.sync.DefaultSyncService;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.ApiUserService;
import org.solovyev.android.messenger.users.DefaultUserService;
import org.solovyev.android.messenger.users.UserService;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 10:27 PM
 */
public class DefaultServiceLocator implements ServiceLocator {

    private AuthService authService;

    private AuthServiceFacade authServiceFacade;

    @NotNull
    private final DefaultUserService userService = new DefaultUserService();

    @NotNull
    private final DefaultChatService chatService = new DefaultChatService();

    @NotNull
    private final DefaultChatMessageService chatMessageService = new DefaultChatMessageService();

    private SyncService syncService;

    @NotNull
    private ApiUserService apiUserService;

    @NotNull
    private ApiChatService apiChatService;

    @NotNull
    private ApiLongPollService apiLongPollService;

    @NotNull
    private RemoteFileService remoteFileService;

    @NotNull
    private RegistrationService registrationService;

    public DefaultServiceLocator(@NotNull Context context,
                                 @NotNull ApiUserService apiUserService,
                                 @NotNull ApiChatService apiChatService,
                                 @NotNull ApiLongPollService apiLongPollService,
                                 @NotNull RegistrationService registrationService) {
        this.apiUserService = apiUserService;
        this.apiChatService = apiChatService;
        this.apiLongPollService = apiLongPollService;
        this.registrationService = registrationService;
        this.remoteFileService = new HttpRemoteFileService(context, MessengerConfigurationImpl.getInstance().getRealm());

        userService.addUserEventListener(chatService);
        chatService.addChatEventListener(userService);
    }

    @Override
    @NotNull
    public ApiUserService getApiUserService() {
        return apiUserService;
    }

    @Override
    @NotNull
    public ApiChatService getApiChatService() {
        return apiChatService;
    }

    @NotNull
    @Override
    public ApiLongPollService getApiLongPollService() {
        return apiLongPollService;
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

    @NotNull
    @Override
    public synchronized AuthServiceFacade getAuthServiceFacade() {
        if (authServiceFacade == null) {
            authServiceFacade = new AuthServiceFacadeImpl();
        }
        return authServiceFacade;
    }
}
