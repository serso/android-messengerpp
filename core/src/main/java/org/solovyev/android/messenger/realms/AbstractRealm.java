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

package org.solovyev.android.messenger.realms;

import android.content.Context;
import com.google.common.base.Splitter;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.users.BaseEditUserFragment;
import org.solovyev.android.messenger.users.Gender;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.properties.Properties.newProperty;
import static org.solovyev.common.text.Strings.isEmpty;

public abstract class AbstractRealm<C extends AccountConfiguration> implements Realm<C> {

	@Nonnull
	private final String id;

	private final int nameResId;

	private final int iconResId;

	@Nonnull
	private final Class<? extends BaseAccountConfigurationFragment<?>> configurationFragmentClass;

	@Nonnull
	private final Class<? extends C> configurationClass;

	private final boolean shouldWaitForDeliveryReport;

	@Nullable
	private final Class<? extends BaseEditUserFragment<?>> createUserFragmentClass;

	private final boolean internetConnectionRequired;

	protected AbstractRealm(@Nonnull String id,
							int nameResId,
							int iconResId,
							@Nonnull Class<? extends BaseAccountConfigurationFragment<?>> configurationFragmentClass,
							@Nonnull Class<? extends C> configurationClass,
							boolean shouldWaitForDeliveryReport,
							@Nullable Class<? extends BaseEditUserFragment<?>> createUserFragmentClass,
							boolean internetConnectionRequired) {
		this.id = id;
		this.nameResId = nameResId;
		this.iconResId = iconResId;
		this.configurationFragmentClass = configurationFragmentClass;
		this.configurationClass = configurationClass;
		this.shouldWaitForDeliveryReport = shouldWaitForDeliveryReport;
		this.createUserFragmentClass = createUserFragmentClass;
		this.internetConnectionRequired = internetConnectionRequired;
	}

	@Nonnull
	@Override
	public final String getId() {
		return this.id;
	}

	@Override
	public final int getNameResId() {
		return this.nameResId;
	}

	@Override
	public final int getIconResId() {
		return this.iconResId;
	}

	@Override
	@Nonnull
	public final Class<? extends C> getConfigurationClass() {
		return configurationClass;
	}

	@Nonnull
	@Override
	public final Class<? extends BaseAccountConfigurationFragment> getConfigurationFragmentClass() {
		return this.configurationFragmentClass;
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof AbstractRealm)) {
			return false;
		}

		final AbstractRealm that = (AbstractRealm) o;

		return id.equals(that.id);

	}

	@Override
	public final int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	protected final void addUserProperty(@Nonnull Context context, @Nonnull List<AProperty> properties, int propertyNameResId, @Nullable String propertyValue) {
		if (!isEmpty(propertyValue)) {
			properties.add(newProperty(context.getString(propertyNameResId), propertyValue));
		}
	}

	@Override
	public void init(@Nonnull Context context) {
	}

	@Nonnull
	@Override
	public List<AProperty> getUserDisplayProperties(@Nonnull User user, @Nonnull Context context) {
		final List<AProperty> result = new ArrayList<AProperty>(user.getPropertiesCollection().size());

		addUserProperty(context, result, R.string.mpp_name, user.getDisplayName());

		final String nickname = user.getPropertyValueByName(User.PROPERTY_NICKNAME);
		if (!isEmpty(nickname)) {
			addUserProperty(context, result, R.string.mpp_nickname, nickname);
		}

		final String sex = user.getPropertyValueByName(User.PROPERTY_SEX);
		if (!isEmpty(sex)) {
			addUserProperty(context, result, R.string.mpp_sex, context.getString(Gender.valueOf(sex).getCaptionResId()));
		}

		final String defaultPhone = user.getPropertyValueByName(User.PROPERTY_PHONE);
		if (!isEmpty(defaultPhone)) {
			addUserProperty(context, result, R.string.mpp_default_phone, defaultPhone);
		}

		final String phones = user.getPropertyValueByName(User.PROPERTY_PHONES);
		if (!isEmpty(phones)) {
			for (String phone : Splitter.on(User.PROPERTY_PHONES_SEPARATOR).split(phones)) {
				addUserProperty(context, result, R.string.mpp_phone, phone);
			}
		}

		return result;
	}


	@Override
	public boolean shouldWaitForDeliveryReport() {
		return shouldWaitForDeliveryReport;
	}

	@Override
	public boolean handleException(@Nonnull Throwable e, @Nonnull Account account) {
		return false;
	}

	@Override
	public boolean canCreateUsers() {
		return createUserFragmentClass != null;
	}

	@Override
	public boolean canEditUsers() {
		return canCreateUsers();
	}

	@Nullable
	@Override
	public Class<? extends BaseEditUserFragment> getCreateUserFragmentClass() {
		return createUserFragmentClass;
	}

	@Override
	public boolean isInternetConnectionRequired() {
		return internetConnectionRequired;
	}
}
