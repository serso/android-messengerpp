package org.solovyev.android.messenger.sync;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.MessengerConfigurationImpl;
import org.solovyev.android.messenger.security.AuthServiceFacade;
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
        public boolean isTime(@NotNull Context context) {
            boolean result = false;

            try {
                final User user = getAuthServiceFacade().getUser(context);
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
        public void doTask(@NotNull Context context) {
            try {
                final User user = getAuthServiceFacade().getUser(context);
                MessengerConfigurationImpl.getInstance().getServiceLocator().getUserService().syncUserProperties(user.getId(), context);
            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }
        }
    },

    user_friends {
        @Override
        public boolean isTime(@NotNull Context context) {
            boolean result = false;

            try {
                final User user = getAuthServiceFacade().getUser(context);
                final DateTime lastFriendsSyncDate = user.getUserSyncData().getLastFriendsSyncDate();
                if (lastFriendsSyncDate == null || lastFriendsSyncDate.plusHours(1).isBefore(DateTime.now())) {
                    result = true;
                }
            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }

            return result;
        }

        @Override
        public void doTask(@NotNull Context context) {
            try {
                final User user = getAuthServiceFacade().getUser(context);
                MessengerConfigurationImpl.getInstance().getServiceLocator().getUserService().syncUserFriends(user.getId(), context);
            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }
        }
    },

    user_icons {

        @Override
        public boolean isTime(@NotNull Context context) {
            boolean result = false;

            try {
                final User user = getAuthServiceFacade().getUser(context);
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
        public void doTask(@NotNull Context context) {
            try {
                final User user = getAuthServiceFacade().getUser(context);

                MessengerConfigurationImpl.getInstance().getServiceLocator().getUserService().fetchUserIcons(user, context);

            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }
        }
    },

    check_online_user_friends {
        @Override
        public boolean isTime(@NotNull Context context) {
            return true;
        }

        @Override
        public void doTask(@NotNull Context context) {
            try {
                final User user = getAuthServiceFacade().getUser(context);
                MessengerConfigurationImpl.getInstance().getServiceLocator().getUserService().checkOnlineUserFriends(user.getId(), context);
            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }
        }
    },

    user_chats {
        @Override
        public boolean isTime(@NotNull Context context) {
            return true;
        }

        @Override
        public void doTask(@NotNull Context context) {
            try {
                final User user = getAuthServiceFacade().getUser(context);
                MessengerConfigurationImpl.getInstance().getServiceLocator().getUserService().syncUserChats(user.getId(), context);
            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }
        }
    },

    chat_messages {
        @Override
        public boolean isTime(@NotNull Context context) {
            return true;
        }

        @Override
        public void doTask(@NotNull Context context) {
            try {
                final User user = getAuthServiceFacade().getUser(context);
                MessengerConfigurationImpl.getInstance().getServiceLocator().getChatService().syncChatMessages(user.getId(), context);
            } catch (UserIsNotLoggedInException e) {
                // ok, user is not logged in
            }
        }
    };

    @NotNull
    private static AuthServiceFacade getAuthServiceFacade() {
        return MessengerConfigurationImpl.getInstance().getServiceLocator().getAuthServiceFacade();
    }

    public abstract boolean isTime(@NotNull Context context);

    public abstract void doTask(@NotNull Context context);
}
