/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.widget.Toast;
import com.google.inject.Inject;
import org.solovyev.android.Threads;
import org.solovyev.android.messenger.accounts.AccountAlreadyExistsException;
import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.connection.AccountConnectionsService;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.messages.MessageService;
import org.solovyev.android.messenger.messages.UnreadMessagesCounter;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.realms.UnsupportedRealmException;
import org.solovyev.android.messenger.realms.test.TestAccountBuilder;
import org.solovyev.android.messenger.realms.test.TestAccountConfiguration;
import org.solovyev.android.messenger.realms.test.TestRealm;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.security.MessengerSecurityService;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.network.NetworkStateService;
import org.solovyev.android.wizard.Wizards;
import org.solovyev.tasks.TaskService;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

import static android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static org.solovyev.android.Android.enableComponent;

public final class App implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Nonnull
	public static final String TAG = "M++";

	@Nonnull
	public static final String TAG_TIME = App.newTag("Time");

	private static final boolean MONKEY_RUNNER = false;

	public static final String CROWDIN_URL = "http://crowdin.net/project/messengerpp";
	public static final String GITHUB_URL = "https://github.com/serso/android-messengerpp";

	@Nonnull
	private static App instance = new App();

	@Nonnull
	private Application application;

	@Inject
	@Nonnull
	private Background background;

	@Inject
	@Nonnull
	private MessageService messageService;

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
	private AccountService accountService;

	@Inject
	@Nonnull
	private RealmService realmService;

	@Inject
	@Nonnull
	private AccountConnectionsService accountConnectionsService;

	@Inject
	@Nonnull
	private UnreadMessagesCounter unreadMessagesCounter;

	@Inject
	@Nonnull
	private UnreadMessagesNotifier unreadMessagesNotifier;

	@Inject
	@Nonnull
	private ExceptionHandler exceptionHandler;

	@Inject
	@Nonnull
	private NotificationService notificationService;

	@Inject
	@Nonnull
	private NetworkStateService networkStateService;

	@Inject
	@Nonnull
	private TaskService taskService;

	@Inject
	@Nonnull
	private MessengerSecurityService securityService;

	@Inject
	@Nonnull
	private Wizards wizards;

	@Nonnull
	private Handler uiHandler;

	@Nonnull
	private MessengerTheme theme;

	private void init0(@Nonnull Application application) {
		this.application = application;
		this.uiHandler = Threads.newUiHandler();

		final SharedPreferences preferences = getPreferences();
		preferences.registerOnSharedPreferenceChangeListener(this);
		theme = getThemeFromPreferences();

		RoboGuice.getBaseApplicationInjector(application).injectMembers(this);

		// init services
		realmService.init();
		accountService.init();
		userService.init();
		chatService.init();
		messageService.init();
		syncService.init();
		unreadMessagesNotifier.init();
		unreadMessagesCounter.init();

		if (isMonkeyRunner()) {
			prepareMonkeyRunnerData();
		}

		// must be done after all loadings
		accountConnectionsService.init();

		networkStateService.startListening(application);
	}

	private void prepareMonkeyRunnerData() {
		accountService.removeAllAccounts();

		try {
			final Realm<? extends AccountConfiguration> realm = realmService.getRealmById(TestRealm.REALM_ID);
			accountService.saveAccount(new TestAccountBuilder(realm, new TestAccountConfiguration(), null));
		} catch (UnsupportedRealmException e) {
		} catch (InvalidCredentialsException e) {
		} catch (AccountAlreadyExistsException e) {
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
		if (MessengerPreferences.Gui.theme.isSameKey(key)) {
			theme = MessengerPreferences.Gui.theme.getPreferenceNoError(preferences);
		} else if (MessengerPreferences.startOnBoot.isSameKey(key)) {
			final Boolean startOnBoot = MessengerPreferences.startOnBoot.getPreference(preferences);
			enableComponent(application, OnBootBroadcastReceiver.class, startOnBoot);
		} else if (MessengerPreferences.isOngoingNotificationEnabled.isSameKey(key)) {
            startBackgroundService();
        }
	}
	/*
	**********************************************************************
	*
	*                           STATIC
	*
	**********************************************************************
	*/

	@Nonnull
	public static String newTag(@Nonnull String tag) {
		return newSubTag(TAG, tag);
	}

	@Nonnull
	public static String newSubTag(@Nonnull String tag, @Nonnull String subTag) {
		return tag + "/" + subTag;
	}

	public static void init(@Nonnull Application application) {
		instance.init0(application);
	}

	public static void showToast(int textResId) {
		Toast.makeText(getApplication(), textResId, Toast.LENGTH_SHORT).show();
	}

	@Nonnull
	public static MessageService getMessageService() {
		return instance.messageService;
	}

	@Nonnull
	public static UserService getUserService() {
		return instance.userService;
	}

	@Nonnull
	public static ChatService getChatService() {
		return instance.chatService;
	}

	@Nonnull
	public static SyncService getSyncService() {
		return instance.syncService;
	}

	@Nonnull
	public static AccountService getAccountService() {
		return instance.accountService;
	}

	@Nonnull
	public static RealmService getRealmService() {
		return instance.realmService;
	}

	@Nonnull
	public static AccountConnectionsService getAccountConnectionsService() {
		return instance.accountConnectionsService;
	}

	@Nonnull
	public static UnreadMessagesCounter getUnreadMessagesCounter() {
		return instance.unreadMessagesCounter;
	}

	@Nonnull
	public static UnreadMessagesNotifier getUnreadMessagesNotifier() {
		return instance.unreadMessagesNotifier;
	}

	@Nonnull
	public static ExceptionHandler getExceptionHandler() {
		return instance.exceptionHandler;
	}

	@Nonnull
	public static NotificationService getNotificationService() {
		return instance.notificationService;
	}

	@Nonnull
	public static NetworkStateService getNetworkStateService() {
		return instance.networkStateService;
	}

	@Nonnull
	public static TaskService getTaskService() {
		return instance.taskService;
	}

	@Nonnull
	public static MessengerSecurityService getSecurityService() {
		return instance.securityService;
	}

	@Nonnull
	public static Application getApplication() {
		return instance.application;
	}

	@Nonnull
	public static Background getBackground() {
		return instance.background;
	}

	public static void exit(Activity activity) {
		getAccountConnectionsService().tryStopAll();

		final Intent serviceIntent = new Intent();
		serviceIntent.setClass(instance.application, StickyService.class);
		instance.application.stopService(serviceIntent);

		activity.finish();
	}

	@Nonnull
	public static EventManager getEventManager(@Nonnull Context context) {
		return RoboGuice.getInjector(context).getInstance(EventManager.class);
	}

	@Nonnull
	public static SharedPreferences getPreferences() {
		return getDefaultSharedPreferences(getApplication());
	}

	@Nonnull
	public static Handler getUiHandler() {
		return instance.uiHandler;
	}

	@Nonnull
	public static Class<? extends Activity> getMainActivityClass() {
		return MainActivity.class;
	}

	@Nonnull
	public static MessengerTheme getTheme() {
		return instance.theme;
	}

	public static Wizards getWizards() {
		return instance.wizards;
	}

	@Nonnull
	public static MessengerTheme getThemeFromPreferences() {
		return MessengerPreferences.Gui.theme.getPreferenceNoError(getPreferences());
	}

	public static void startBackgroundService() {
        final Intent serviceIntent = new Intent();
        serviceIntent.setClass(instance.application, StickyService.class);
        instance.application.startService(serviceIntent);
    }

	public static boolean isDebuggable() {
		final ApplicationInfo applicationInfo = getApplication().getApplicationInfo();
		return (applicationInfo.flags & FLAG_DEBUGGABLE) == FLAG_DEBUGGABLE;
	}

	public static boolean isMonkeyRunner() {
		// NOTE: this code is only for monkeyrunner
		return MONKEY_RUNNER;
	}

	public static void executeInBackground(@Nonnull final Runnable runnable) {
		instance.background.execute(new Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Exception e) {
					instance.exceptionHandler.handleException(e);
				}
			}
		});
	}

	/*
	**********************************************************************
	*
	*                           FOR TESTS ONLY
	*
	**********************************************************************
	*/

	@Nonnull
	static App getInstance() {
		return instance;
	}

	public void setApplication(@Nonnull Application application) {
		this.application = application;
	}

	public void setMessageService(@Nonnull MessageService messageService) {
		this.messageService = messageService;
	}

	public void setUserService(@Nonnull UserService userService) {
		this.userService = userService;
	}

	public void setChatService(@Nonnull ChatService chatService) {
		this.chatService = chatService;
	}

	public void setSyncService(@Nonnull SyncService syncService) {
		this.syncService = syncService;
	}

	public void setAccountService(@Nonnull AccountService accountService) {
		this.accountService = accountService;
	}

	public void setRealmService(@Nonnull RealmService realmService) {
		this.realmService = realmService;
	}

	public void setAccountConnectionsService(@Nonnull AccountConnectionsService accountConnectionsService) {
		this.accountConnectionsService = accountConnectionsService;
	}

	public void setUnreadMessagesCounter(@Nonnull UnreadMessagesCounter unreadMessagesCounter) {
		this.unreadMessagesCounter = unreadMessagesCounter;
	}

	public void setUnreadMessagesNotifier(@Nonnull UnreadMessagesNotifier unreadMessagesNotifier) {
		this.unreadMessagesNotifier = unreadMessagesNotifier;
	}

	public void setExceptionHandler(@Nonnull ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	public void setNotificationService(@Nonnull NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public void setNetworkStateService(@Nonnull NetworkStateService networkStateService) {
		this.networkStateService = networkStateService;
	}

	public void setTaskService(@Nonnull TaskService taskService) {
		this.taskService = taskService;
	}

	public void setSecurityService(@Nonnull MessengerSecurityService securityService) {
		this.securityService = securityService;
	}

}
