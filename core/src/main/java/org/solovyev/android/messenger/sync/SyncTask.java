package org.solovyev.android.messenger.sync;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.AbstractMessengerApplication;
import org.solovyev.android.messenger.security.AuthService;
import org.solovyev.android.messenger.security.UserIsNotLoggedInException;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 11:18 PM
 */
public enum SyncTask {

    user_properties {
        @Override
        public boolean isTime(@NotNull SyncData syncData, @NotNull Context context) {
            boolean result = false;

            try {
                final User user = getAuthService().getUser(syncData.getRealmId(), context);
                final DateTime lastPropertiesSyncDate = user.getUserSyncData().getLastPropertiesSyncDate();
                if (lastPropertiesSyncDate == null || lastPropertiesSyncDate.plusHours(1).isBefore(DateTime.now())) {
                    result = true;
                }
            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }

            return result;
        }

        @Override
        public void doTask(@NotNull SyncData syncData, @NotNull Context context) {
            try {
                final User user = getAuthService().getUser(syncData.getRealmId(), context);
                AbstractMessengerApplication.getServiceLocator().getUserService().syncUserProperties(user.getId(), context);
            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }
        }
    },

    user_contacts {
        @Override
        public boolean isTime(@NotNull SyncData syncData, @NotNull Context context) {
            boolean result = false;

            try {
                final User user = getAuthService().getUser(syncData.getRealmId(), context);
                final DateTime lastContactsSyncDate = user.getUserSyncData().getLastContactsSyncDate();
                if (lastContactsSyncDate == null || lastContactsSyncDate.plusHours(1).isBefore(DateTime.now())) {
                    result = true;
                }
            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }

            return result;
        }

        @Override
        public void doTask(@NotNull SyncData syncData, @NotNull Context context) {
            try {
                final User user = getAuthService().getUser(syncData.getRealmId(), context);
                AbstractMessengerApplication.getServiceLocator().getUserService().syncUserContacts(user.getId(), context);
            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }
        }
    },

    user_icons {

        @Override
        public boolean isTime(@NotNull SyncData syncData, @NotNull Context context) {
            boolean result = false;

            try {
                final User user = getAuthService().getUser(syncData.getRealmId(), context);
                final DateTime lastUserIconsSyncDate = user.getUserSyncData().getLastUserIconsSyncData();
                if (lastUserIconsSyncDate == null || lastUserIconsSyncDate.plusDays(1).isBefore(DateTime.now())) {
                    result = true;
                }
            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }

            return result;
        }

        @Override
        public void doTask(@NotNull SyncData syncData, @NotNull Context context) {
            try {
                final User user = getAuthService().getUser(syncData.getRealmId(), context);

                AbstractMessengerApplication.getServiceLocator().getUserService().fetchUserIcons(user, context);

            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }
        }
    },

    check_online_user_contacts {
        @Override
        public boolean isTime(@NotNull SyncData syncData, @NotNull Context context) {
            return true;
        }

        @Override
        public void doTask(@NotNull SyncData syncData, @NotNull Context context) {
            try {
                final User user = getAuthService().getUser(syncData.getRealmId(), context);
                AbstractMessengerApplication.getServiceLocator().getUserService().checkOnlineUserContacts(user.getId(), context);
            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }
        }
    },

    user_chats {
        @Override
        public boolean isTime(@NotNull SyncData syncData, @NotNull Context context) {
            boolean result = false;

            try {
                final User user = getAuthService().getUser(syncData.getRealmId(), context);
                final DateTime lastChatsSyncDate = user.getUserSyncData().getLastChatsSyncDate();
                if (lastChatsSyncDate == null || lastChatsSyncDate.plusHours(24).isBefore(DateTime.now())) {
                    result = true;
                }
            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }

            return result;
        }

        @Override
        public void doTask(@NotNull SyncData syncData, @NotNull Context context) {
            try {
                final User user = getAuthService().getUser(syncData.getRealmId(), context);
                AbstractMessengerApplication.getServiceLocator().getUserService().syncUserChats(user.getId(), context);
            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }
        }
    },

    chat_messages {
        @Override
        public boolean isTime(@NotNull SyncData syncData, @NotNull Context context) {
            return true;
        }

        @Override
        public void doTask(@NotNull SyncData syncData, @NotNull Context context) {
            try {
                final User user = getAuthService().getUser(syncData.getRealmId(), context);
                AbstractMessengerApplication.getServiceLocator().getChatService().syncChatMessages(user.getId(), context);
            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }
        }
    };

    @NotNull
    private static AuthService getAuthService() {
        return AbstractMessengerApplication.getServiceLocator().getAuthService();
    }

    public abstract boolean isTime(@NotNull SyncData syncData, @NotNull Context context);

    public abstract void doTask(@NotNull SyncData syncData, @NotNull Context context);
}
