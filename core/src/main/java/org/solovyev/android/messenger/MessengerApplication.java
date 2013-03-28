package org.solovyev.android.messenger;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import com.google.inject.Inject;
import org.joda.time.DateTimeZone;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.messages.UnreadMessagesCounter;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.network.NetworkStateService;
import org.solovyev.common.datetime.FastDateTimeZoneProvider;
import roboguice.RoboGuice;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:16 PM
 */
public class MessengerApplication extends Application implements MessengerServiceLocator {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

    @Nonnull
    private static MessengerApplication instance;

    @Nonnull
    public static final String TAG = "M++";

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @Nonnull
    private ChatMessageService chatMessageService;

    @Inject
    @Nonnull
    private UserService userService;

    @Inject
    @Nonnull
    private ChatService chatService;

    @Inject
    @Nonnull
    private SyncService syncService;

    @Inject
    @Nonnull
    private RealmService realmService;

    @Inject
    @Nonnull
    private UnreadMessagesCounter unreadMessagesCounter;

    @Inject
    @Nonnull
    private UnreadMessagesNotifier unreadMessagesNotifier;

    @Inject
    @Nonnull
    private MessengerExceptionHandler exceptionHandler;

    @Inject
    @Nonnull
    private NotificationService notificationService;

    @Inject
    @Nonnull
    private NetworkStateService networkStateService;

    public MessengerApplication() {
        instance = this;
    }

    @Nonnull
    public static MessengerServiceLocator getServiceLocator() {
        return instance;
    }

    @Nonnull
    public static MessengerApplication getApp() {
        return instance;
    }

    @Override
    @Nonnull
    public ChatMessageService getChatMessageService() {
        return chatMessageService;
    }

    @Override
    @Nonnull
    public UserService getUserService() {
        return userService;
    }

    @Override
    @Nonnull
    public ChatService getChatService() {
        return chatService;
    }

    @Override
    @Nonnull
    public SyncService getSyncService() {
        return syncService;
    }

    @Override
    @Nonnull
    public RealmService getRealmService() {
        return realmService;
    }

    @Override
    @Nonnull
    public NetworkStateService getNetworkStateService() {
        return networkStateService;
    }

    @Nonnull
    @Override
    public MessengerExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    @Nonnull
    @Override
    public UnreadMessagesCounter getUnreadMessagesCounter() {
        return unreadMessagesCounter;
    }

    @Nonnull
    @Override
    public NotificationService getNotificationService() {
        return notificationService;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize Joda time for android
        System.setProperty("org.joda.time.DateTimeZone.Provider", FastDateTimeZoneProvider.class.getName());

        DateTimeZone.setDefault(DateTimeZone.UTC);

        MessengerPreferences.setDefaultValues(this);

        RoboGuice.getBaseApplicationInjector(this).injectMembers(this);

        // init services
        this.realmService.init();
        this.userService.init();
        this.chatService.init();
        this.chatMessageService.init();
        this.syncService.init();
        this.unreadMessagesCounter.init();

        // load persistence data
        this.realmService.load();

        this.networkStateService.startListening(this);
    }

    public void exit(@Nonnull Activity activity) {
        realmService.stopAllRealmConnections();

        final Intent serviceIntent = new Intent();
        serviceIntent.setClass(this, MessengerService.class);
        stopService(serviceIntent);

        activity.finish();
    }
}
