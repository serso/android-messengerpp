package org.solovyev.android.messenger.users;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import javax.annotation.Nonnull;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.realms.RealmEntityImpl;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.APropertyImpl;
import org.solovyev.common.JObject;
import org.solovyev.common.text.Strings;

import javax.annotation.Nullable;
import java.util.*;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 10:30 PM
 */
public class UserImpl extends JObject implements User {

    @Nonnull
    private String login;

    @Nonnull
    private RealmEntity realmEntity;

    @Nonnull
    private UserSyncData userSyncData;

    @Nonnull
    private List<AProperty> properties = new ArrayList<AProperty>();

    @Nonnull
    private Map<String, String> propertiesMap = new HashMap<String, String>();

    private UserImpl() {
    }

    @Nonnull
    public static User newInstance(@Nonnull String reamId,
                                   @Nonnull String realmUserId,
                                   @Nonnull UserSyncData userSyncData,
                                   @Nonnull List<AProperty> properties) {
        final RealmEntity realmEntity = RealmEntityImpl.newInstance(reamId, realmUserId);
        return newInstance(realmEntity, userSyncData, properties);
    }

    @Nonnull
    public static User newInstance(@Nonnull RealmEntity realmEntity,
                                   @Nonnull UserSyncData userSyncData,
                                   @Nonnull List<AProperty> properties) {
        final UserImpl result = new UserImpl();

        result.realmEntity = realmEntity;
        result.login = realmEntity.getRealmEntityId();
        result.userSyncData = userSyncData;
        result.properties.addAll(properties);

        for (AProperty property : result.properties) {
            result.propertiesMap.put(property.getName(), property.getValue());
        }

        return result;
    }

    @Nonnull
    public static User newFakeInstance(@Nonnull RealmEntity realmUser) {
        return newInstance(realmUser, UserSyncDataImpl.newNeverSyncedInstance(), Collections.<AProperty>emptyList());
    }

    @Nonnull
    public static User newFakeInstance(@Nonnull String userId) {
        return newFakeInstance(RealmEntityImpl.fromEntityId(userId));
    }

    @Nonnull
    public String getLogin() {
        return login;
    }

    @Nonnull
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
        return Boolean.valueOf(getPropertyValueByName(PROPERTY_ONLINE));
    }

    @Override
    @Nonnull
    public UserSyncData getUserSyncData() {
        return userSyncData;
    }

    @Nonnull
    @Override
    public User updateChatsSyncDate() {
        final UserImpl clone = this.clone();
        clone.userSyncData = clone.userSyncData.updateChatsSyncDate();
        return clone;
    }

    @Nonnull
    @Override
    public User updatePropertiesSyncDate() {
        final UserImpl clone = this.clone();
        clone.userSyncData = clone.userSyncData.updatePropertiesSyncDate();
        return clone;
    }

    @Nonnull
    @Override
    public User updateContactsSyncDate() {
        final UserImpl clone = this.clone();
        clone.userSyncData = clone.userSyncData.updateContactsSyncDate();
        return clone;
    }

    @Nonnull
    @Override
    public User updateUserIconsSyncDate() {
        final UserImpl clone = this.clone();
        clone.userSyncData = clone.userSyncData.updateUserIconsSyncDate();
        return clone;
    }

    @Override
    @Nonnull
    public List<AProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Nonnull
    @Override
    public RealmEntity getRealmUser() {
        return this.realmEntity;
    }

    @Override
    public String getPropertyValueByName(@Nonnull String name) {
        return this.propertiesMap.get(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserImpl)) return false;

        final UserImpl that = (UserImpl) o;

        if (!realmEntity.equals(that.realmEntity)) return false;

        return true;
    }


    @Override
    public int hashCode() {
        return realmEntity.hashCode();
    }

    @Override
    public String toString() {
        return "UserImpl{" +
                "id=" + realmEntity.getEntityId() +
                '}';
    }

    @Nonnull
    @Override
    public UserImpl clone() {
        final UserImpl clone = (UserImpl) super.clone();

        clone.realmEntity = realmEntity.clone();

        return clone;
    }

    @Nonnull
    @Override
    public User cloneWithNewStatus(boolean online) {
        final UserImpl clone = clone();

        Iterables.removeIf(clone.properties, new Predicate<AProperty>() {
            @Override
            public boolean apply(@Nullable AProperty property) {
                return property != null && property.getName().equals(PROPERTY_ONLINE);
            }
        });
        clone.properties.add(APropertyImpl.newInstance(PROPERTY_ONLINE, Boolean.toString(online)));
        clone.propertiesMap.put(PROPERTY_ONLINE, Boolean.toString(online));

        return clone;
    }
}
