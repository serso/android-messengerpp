package org.solovyev.android.messenger.users;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.common.JObject;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 10:43 PM
 */
public class UserSyncDataImpl extends JObject implements UserSyncData {

    @Nullable
    private DateTime lastPropertiesSyncDate;

    @Nullable
    private DateTime lastFriendsSyncDate;

    @Nullable
    private DateTime lastChatsSyncDate;

    @Nullable
    private DateTime lastUserIconsSyncDate;

    private UserSyncDataImpl() {
    }

    private UserSyncDataImpl(@Nullable DateTime lastPropertiesSyncDate,
                             @Nullable DateTime lastFriendsSyncDate,
                             @Nullable DateTime lastChatsSyncDate,
                             @Nullable DateTime lastUserIconsSyncDate) {
        this.lastPropertiesSyncDate = lastPropertiesSyncDate;
        this.lastFriendsSyncDate = lastFriendsSyncDate;
        this.lastChatsSyncDate = lastChatsSyncDate;
        this.lastUserIconsSyncDate = lastUserIconsSyncDate;
    }


    @NotNull
    public static UserSyncDataImpl newInstance(@Nullable DateTime lastPropertiesSyncDate,
                                               @Nullable DateTime lastFriendsSyncDate,
                                               @Nullable DateTime lastChatsSyncDate,
                                               @Nullable DateTime lastUserIconsSyncDate) {
        return new UserSyncDataImpl(lastPropertiesSyncDate, lastFriendsSyncDate, lastChatsSyncDate, lastUserIconsSyncDate);
    }

    @NotNull
    public static UserSyncDataImpl copyOf(@NotNull UserSyncData userSyncData) {
        return new UserSyncDataImpl(userSyncData.getLastPropertiesSyncDate(), userSyncData.getLastFriendsSyncDate(), userSyncData.getLastChatsSyncDate(), userSyncData.getLastUserIconsSyncData());
    }

    @NotNull
    public static UserSyncDataImpl newInstanceFromStrings(@Nullable String lastPropertiesSyncDateString,
                                                          @Nullable String lastFriendsSyncDateString,
                                                          @Nullable String lastChatsSyncDateString,
                                                          @Nullable String lastUserIconsSyncDateString) {

        final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();
        final DateTime lastPropertiesSyncDate = lastPropertiesSyncDateString == null ? null : dateTimeFormatter.parseDateTime(lastPropertiesSyncDateString);
        final DateTime lastFriendsSyncDate = lastFriendsSyncDateString == null ? null : dateTimeFormatter.parseDateTime(lastFriendsSyncDateString);
        final DateTime lastChatsSyncDate = lastChatsSyncDateString == null ? null : dateTimeFormatter.parseDateTime(lastChatsSyncDateString);
        final DateTime lastUserIconsSyncDate = lastUserIconsSyncDateString == null ? null : dateTimeFormatter.parseDateTime(lastUserIconsSyncDateString);
        return UserSyncDataImpl.newInstance(lastPropertiesSyncDate, lastFriendsSyncDate, lastChatsSyncDate, lastUserIconsSyncDate);
    }

    @Override
    @Nullable
    public DateTime getLastFriendsSyncDate() {
        return lastFriendsSyncDate;
    }

    @Nullable
    @Override
    public DateTime getLastChatsSyncDate() {
        return lastChatsSyncDate;
    }

    @Override
    public DateTime getLastUserIconsSyncData() {
        return lastUserIconsSyncDate;
    }

    @NotNull
    @Override
    public UserSyncData updateChatsSyncDate() {
        final UserSyncDataImpl clone = this.clone();
        clone.lastChatsSyncDate = DateTime.now();
        return clone;
    }

    @NotNull
    @Override
    public UserSyncData updatePropertiesSyncDate() {
        final UserSyncDataImpl clone = this.clone();
        clone.lastPropertiesSyncDate = DateTime.now();
        return clone;
    }

    @NotNull
    @Override
    public UserSyncData updateFriendsSyncDate() {
        final UserSyncDataImpl clone = this.clone();
        clone.lastFriendsSyncDate = DateTime.now();
        return clone;
    }

    @NotNull
    @Override
    public UserSyncData updateUserIconsSyncData() {
        final UserSyncDataImpl clone = this.clone();
        clone.lastUserIconsSyncDate = DateTime.now();
        return clone;    }

    @Override
    @Nullable
    public DateTime getLastPropertiesSyncDate() {
        return lastPropertiesSyncDate;
    }

    @NotNull
    @Override
    public UserSyncDataImpl clone() {
        // dates are immutable => can leave links as is
        return (UserSyncDataImpl) super.clone();
    }
}
