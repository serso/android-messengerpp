package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.AbstractMessengerEntity;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.MutableAProperties;
import org.solovyev.android.properties.Properties;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 10:30 PM
 */
final class UserImpl extends AbstractMessengerEntity implements User {

	@Nonnull
	private String login;

	@Nonnull
	private UserSyncData userSyncData;

	@Nonnull
	private MutableAProperties properties;

	@Nullable
	private String displayName;

	UserImpl(@Nonnull Entity entity) {
		super(entity);
	}

	@Nonnull
	static User newInstance(@Nonnull Entity entity,
							@Nonnull UserSyncData userSyncData,
							@Nonnull Collection<AProperty> properties) {
		final UserImpl result = new UserImpl(entity);

		result.login = entity.getAccountEntityId();
		result.userSyncData = userSyncData;
		result.properties = Properties.newProperties(properties);

		return result;
	}

	@Nonnull
	public String getLogin() {
		return login;
	}

	@Override
	public Gender getGender() {
		final String result = getPropertyValueByName(User.PROPERTY_SEX);
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
	public String getDisplayName() {
		if (displayName == null) {
			displayName = createDisplayName();
		}
		return displayName;
	}

	@Nonnull
	private String createDisplayName() {
		final StringBuilder result = new StringBuilder();

		final String firstName = this.getPropertyValueByName(User.PROPERTY_FIRST_NAME);
		final String lastName = this.getPropertyValueByName(User.PROPERTY_LAST_NAME);

		boolean firstNameExists = !Strings.isEmpty(firstName);
		boolean lastNameExists = !Strings.isEmpty(lastName);

		if (!firstNameExists && !lastNameExists) {
			// first and last names are empty
			result.append(this.getEntity().getAccountEntityId());
		} else {

			if (firstNameExists) {
				result.append(firstName);
			}

			if (firstNameExists && lastNameExists) {
				result.append(" ");
			}

			if (lastNameExists) {
				result.append(lastName);
			}
		}

		return result.toString();
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
	public Collection<AProperty> getProperties() {
		return properties.getPropertiesCollection();
	}

	@Override
	public String getPropertyValueByName(@Nonnull String name) {
		return this.properties.getPropertyValue(name);
	}

	@Nonnull
	@Override
	public UserImpl clone() {
		final UserImpl clone = (UserImpl) super.clone();
		clone.properties = this.properties.clone();
		return clone;
	}

	@Override
	public String toString() {
		return "UserImpl{" +
				"id=" + getEntity().getEntityId() +
				'}';
	}

	@Nonnull
	@Override
	public User cloneWithNewStatus(boolean online) {
		final UserImpl clone;

		if (isOnline() != online) {
			clone = clone();

			clone.properties.setProperty(PROPERTY_ONLINE, Boolean.toString(online));
		} else {
			clone = this;
		}

		return clone;
	}

	@Nonnull
	@Override
	public User cloneWithNewProperty(@Nonnull AProperty property) {
		final UserImpl clone = clone();

		clone.properties.setProperty(property);

		return clone;
	}
}
