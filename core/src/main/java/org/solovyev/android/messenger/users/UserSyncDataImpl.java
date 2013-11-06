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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.common.JObject;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 10:43 PM
 */
class UserSyncDataImpl extends JObject implements UserSyncData {

	@Nullable
	private DateTime lastPropertiesSyncDate;

	@Nullable
	private DateTime lastContactsSyncDate;

	@Nullable
	private DateTime lastChatsSyncDate;

	@Nullable
	private DateTime lastUserIconsSyncDate;

	private UserSyncDataImpl() {
	}

	private UserSyncDataImpl(@Nullable DateTime lastPropertiesSyncDate,
							 @Nullable DateTime lastContactsSyncDate,
							 @Nullable DateTime lastChatsSyncDate,
							 @Nullable DateTime lastUserIconsSyncDate) {
		this.lastPropertiesSyncDate = lastPropertiesSyncDate;
		this.lastContactsSyncDate = lastContactsSyncDate;
		this.lastChatsSyncDate = lastChatsSyncDate;
		this.lastUserIconsSyncDate = lastUserIconsSyncDate;
	}

	@Nonnull
	static UserSyncDataImpl newNeverSyncedInstance() {
		return new UserSyncDataImpl(null, null, null, null);
	}

	@Nonnull
	static UserSyncDataImpl newInstance(@Nullable DateTime lastPropertiesSyncDate,
										@Nullable DateTime lastContactsSyncDate,
										@Nullable DateTime lastChatsSyncDate,
										@Nullable DateTime lastUserIconsSyncDate) {
		return new UserSyncDataImpl(lastPropertiesSyncDate, lastContactsSyncDate, lastChatsSyncDate, lastUserIconsSyncDate);
	}

	@Nonnull
	static UserSyncDataImpl copyOf(@Nonnull UserSyncData userSyncData) {
		return new UserSyncDataImpl(userSyncData.getLastPropertiesSyncDate(), userSyncData.getLastContactsSyncDate(), userSyncData.getLastChatsSyncDate(), userSyncData.getLastUserIconsSyncData());
	}

	@Nonnull
	static UserSyncDataImpl newInstance(@Nullable String lastPropertiesSyncDateString,
										@Nullable String lastContactsSyncDateString,
										@Nullable String lastChatsSyncDateString,
										@Nullable String lastUserIconsSyncDateString) {

		final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();
		final DateTime lastPropertiesSyncDate = lastPropertiesSyncDateString == null ? null : dateTimeFormatter.parseDateTime(lastPropertiesSyncDateString);
		final DateTime lastContactsSyncDate = lastContactsSyncDateString == null ? null : dateTimeFormatter.parseDateTime(lastContactsSyncDateString);
		final DateTime lastChatsSyncDate = lastChatsSyncDateString == null ? null : dateTimeFormatter.parseDateTime(lastChatsSyncDateString);
		final DateTime lastUserIconsSyncDate = lastUserIconsSyncDateString == null ? null : dateTimeFormatter.parseDateTime(lastUserIconsSyncDateString);
		return UserSyncDataImpl.newInstance(lastPropertiesSyncDate, lastContactsSyncDate, lastChatsSyncDate, lastUserIconsSyncDate);
	}

	@Override
	@Nullable
	public DateTime getLastContactsSyncDate() {
		return lastContactsSyncDate;
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

	@Nonnull
	@Override
	public UserSyncData updateChatsSyncDate() {
		final UserSyncDataImpl clone = this.clone();
		clone.lastChatsSyncDate = DateTime.now();
		return clone;
	}

	@Nonnull
	@Override
	public UserSyncData updatePropertiesSyncDate() {
		final UserSyncDataImpl clone = this.clone();
		clone.lastPropertiesSyncDate = DateTime.now();
		return clone;
	}

	@Nonnull
	@Override
	public UserSyncData updateContactsSyncDate() {
		final UserSyncDataImpl clone = this.clone();
		clone.lastContactsSyncDate = DateTime.now();
		return clone;
	}

	@Nonnull
	@Override
	public UserSyncData updateUserIconsSyncDate() {
		final UserSyncDataImpl clone = this.clone();
		clone.lastUserIconsSyncDate = DateTime.now();
		return clone;
	}

	@Override
	public boolean isFirstSyncDone() {
		return getLastContactsSyncDate() != null;
	}

	@Override
	@Nullable
	public DateTime getLastPropertiesSyncDate() {
		return lastPropertiesSyncDate;
	}

	@Nonnull
	@Override
	public UserSyncDataImpl clone() {
		// dates are immutable => can leave links as is
		return (UserSyncDataImpl) super.clone();
	}
}
