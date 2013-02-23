package org.solovyev.android.messenger.users;

import android.os.Parcel;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.APropertyImpl;
import org.solovyev.common.JObject;
import org.solovyev.common.VersionedEntity;
import org.solovyev.common.VersionedEntityImpl;
import org.solovyev.common.text.Strings;

import javax.annotation.Nullable;
import java.util.*;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 10:30 PM
 */
public class UserImpl extends JObject implements User {

    @NotNull
    private VersionedEntity<String> versionedEntity;

    @NotNull
    private String login;

    @NotNull
    private UserSyncData userSyncData;

    @NotNull
    private List<AProperty> properties = new ArrayList<AProperty>();

    @NotNull
    private Map<String, String> propertiesMap = new HashMap<String, String>();

    private UserImpl() {
    }

    @NotNull
    public static User newInstance(@NotNull VersionedEntity<String> versionedEntity,
                                   @NotNull UserSyncData userSyncData,
                                   @NotNull List<AProperty> properties) {
        final UserImpl result = new UserImpl();

        result.versionedEntity = versionedEntity;
        result.login = String.valueOf(versionedEntity.getId());
        result.userSyncData = userSyncData;
        result.properties.addAll(properties);

        for (AProperty property : result.properties) {
            result.propertiesMap.put(property.getName(), property.getValue());
        }

        return result;
    }

    @NotNull
    public static User newInstance(@NotNull String userId) {
        return newInstance(new VersionedEntityImpl<String>(userId), UserSyncDataImpl.newInstance(null, null, null, null), Collections.<AProperty>emptyList());
    }

    @NotNull
    public static User fromParcel(@NotNull Parcel in) {
        throw new UnsupportedOperationException();
        /*final Integer id = in.readInt();
        final Integer version = in.readInt();
        final String login = in.readString();

        final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();
        final String lastPropertiesSyncDateString = in.readString();
        DateTime lastPropertiesSyncDate = null;
        if ( lastPropertiesSyncDateString != null ) {
            lastPropertiesSyncDate = dateTimeFormatter.parseDateTime(lastPropertiesSyncDateString);
        }

        final String lastFriendsSyncDateString = in.readString();
        DateTime lastFriendsSyncDate = null;
        if ( lastFriendsSyncDateString != null ) {
            lastFriendsSyncDate = dateTimeFormatter.parseDateTime(lastFriendsSyncDateString);
        }

        final List<AProperty> properties = new ArrayList<AProperty>();
        in.readList(properties, Thread.currentThread().getContextClassLoader());

        return newInstance(new VersionedEntityImpl(id, version), lastPropertiesSyncDate, lastFriendsSyncDate, properties);*/
    }

    @Override
    @NotNull
    public String getId() {
        return versionedEntity.getId();
    }

    @Override
    @NotNull
    public Integer getVersion() {
        return versionedEntity.getVersion();
    }

    @NotNull
    public String getLogin() {
        return login;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        final StringBuilder result = new StringBuilder();

        final String firstName = getPropertyValueByName("firstName");
        final String lastName = getPropertyValueByName("lastName");

        result.append(firstName);
        if (!Strings.isEmpty(firstName) && !Strings.isEmpty(lastName)) {
            result.append(" ");
        }
        result.append(lastName);

        return result.toString();
    }

    @Override
    public Gender getGender() {
        final String result = getPropertyValueByName("sex");
        return result == null ? null : Gender.valueOf(result);
    }

    @Override
    public boolean isOnline() {
        return Boolean.valueOf(getPropertyValueByName("online"));
    }

    @Override
    @NotNull
    public UserSyncData getUserSyncData() {
        return userSyncData;
    }

    @NotNull
    @Override
    public User updateChatsSyncDate() {
        final UserImpl clone = this.clone();
        clone.userSyncData = clone.userSyncData.updateChatsSyncDate();
        return clone;
    }

    @NotNull
    @Override
    public User updatePropertiesSyncDate() {
        final UserImpl clone = this.clone();
        clone.userSyncData = clone.userSyncData.updatePropertiesSyncDate();
        return clone;
    }

    @NotNull
    @Override
    public User updateContactsSyncDate() {
        final UserImpl clone = this.clone();
        clone.userSyncData = clone.userSyncData.updateContactsSyncDate();
        return clone;
    }

    @NotNull
    @Override
    public User updateUserIconsSyncDate() {
        final UserImpl clone = this.clone();
        clone.userSyncData = clone.userSyncData.updateUserIconsSyncDate();
        return clone;
    }

    /*    @Override
    public int describeContents() {
        return 0;
    }*/

    @Override
    @NotNull
    public List<AProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Override
    public String getPropertyValueByName(@NotNull String name) {
        return this.propertiesMap.get(name);
    }

/*    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(versionedEntity.getId());
        out.writeInt(versionedEntity.getVersion());
        out.writeString(login);
        final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();
        out.writeString(this.lastPropertiesSyncDate == null ? null : dateTimeFormatter.print(this.lastPropertiesSyncDate));
        out.writeString(this.lastFriendsSyncDate == null ? null : dateTimeFormatter.print(this.lastFriendsSyncDate));
        out.writeList(properties);
    }*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserImpl)) return false;

        UserImpl user = (UserImpl) o;

        if (!versionedEntity.equals(user.versionedEntity)) return false;

        return true;
    }

    @Override
    public boolean equalsVersion(Object that) {
        return this.equals(that) && this.versionedEntity.equalsVersion(((UserImpl) that).versionedEntity);
    }

    @Override
    public int hashCode() {
        return versionedEntity.hashCode();
    }

    @Override
    public String toString() {
        return "UserImpl{" +
                "versionedEntity=" + versionedEntity +
                '}';
    }

    @NotNull
    @Override
    public UserImpl clone() {
        final UserImpl clone = (UserImpl) super.clone();

        clone.versionedEntity = versionedEntity.clone();

        return clone;
    }

    @NotNull
    @Override
    public User cloneWithNewStatus(boolean online) {
        final UserImpl clone = clone();

        Iterables.removeIf(clone.properties, new Predicate<AProperty>() {
            @Override
            public boolean apply(@Nullable AProperty property) {
                return property != null && property.getName().equals("online");
            }
        });
        clone.properties.add(APropertyImpl.newInstance("online", Boolean.toString(online)));
        clone.propertiesMap.put("online", Boolean.toString(online));

        return clone;
    }
}
