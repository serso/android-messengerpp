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

package org.solovyev.android.messenger.users;

import com.google.common.base.Splitter;
import org.solovyev.android.messenger.AbstractIdentifiable;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.properties.AProperties;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.MutableAProperties;
import org.solovyev.android.properties.Properties;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.solovyev.android.messenger.users.Users.newOnlineProperty;

final class UserImpl extends AbstractIdentifiable implements MutableUser {

	@Nonnull
	private String login;

	@Nonnull
	private MutableAProperties properties;

	@Nullable
	private String displayName;

	UserImpl(@Nonnull Entity entity) {
		super(entity);
	}

	@Nonnull
	static MutableUser newInstance(@Nonnull Entity entity,
								   @Nonnull Collection<AProperty> properties) {
		final UserImpl result = new UserImpl(entity);

		result.login = entity.getAccountEntityId();
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

		final String firstName = getFirstName();
		final String lastName = getLastName();

		boolean firstNameExists = !Strings.isEmpty(firstName);
		boolean lastNameExists = !Strings.isEmpty(lastName);

		if (!firstNameExists && !lastNameExists) {
			// first and last names are empty
			final String login = getLogin();
			if (!Strings.isEmpty(login)) {
				result.append(login);
			} else {
				result.append(this.getEntity().getAccountEntityId());
			}
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

	@Override
	@Nullable
	public String getFirstName() {
		return this.getPropertyValueByName(PROPERTY_FIRST_NAME);
	}

	@Override
	@Nullable
	public String getLastName() {
		return this.getPropertyValueByName(PROPERTY_LAST_NAME);
	}

	@Nullable
	@Override
	public String getPhoneNumber() {
		return this.getPropertyValueByName(PROPERTY_PHONE);
	}

	@Nonnull
	@Override
	public Set<String> getPhoneNumbers() {
		final Set<String> phones = new HashSet<String>();

		final String phoneNumber = getPhoneNumber();
		if(phoneNumber != null) {
			phones.add(phoneNumber);
		}

		final String phonesProperty = getPropertyValueByName(PROPERTY_PHONES);
		if (phonesProperty != null) {
			for (String phone: Splitter.on(User.PROPERTY_PHONES_SEPARATOR).omitEmptyStrings().split(phonesProperty)) {
				phones.add(phone);
			}
		}

		return phones;
	}

	@Override
	@Nonnull
	public Collection<AProperty> getPropertiesCollection() {
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

	@Nonnull
	@Override
	public User merge(@Nonnull User that) {
		if(this == that) {
			return this;
		} else {
			final UserImpl clone = this.clone();
			clone.displayName = that.getDisplayName();
			clone.properties.setPropertiesFrom(that.getPropertiesCollection());
			return clone;
		}
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

			clone.properties.setProperty(newOnlineProperty(online));
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

	@Nonnull
	@Override
	public User cloneWithNewProperties(@Nonnull AProperties properties) {
		final UserImpl clone = clone();

		clone.properties.clearProperties();
		clone.properties.setPropertiesFrom(properties.getPropertiesCollection());

		return clone;
	}

	@Nonnull
	@Override
	public MutableAProperties getProperties() {
		return properties;
	}

	@Override
	public void setOnline(boolean online) {
		properties.setProperty(newOnlineProperty(online));
	}

	@Override
	public void setLastName(@Nullable String lastName) {
		if (lastName != null) {
			properties.setProperty(PROPERTY_LAST_NAME, lastName);
		} else {
			properties.removeProperty(PROPERTY_LAST_NAME);
		}
	}

	@Override
	public void setFirstName(@Nullable String firstName) {
		if (firstName != null) {
			properties.setProperty(PROPERTY_FIRST_NAME, firstName);
		} else {
			properties.removeProperty(PROPERTY_FIRST_NAME);
		}
	}
}
