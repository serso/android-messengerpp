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

package org.solovyev.android.messenger.accounts;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.common.JObject;

class AccountSyncDataImpl extends JObject implements MutableAccountSyncData {

	@Nullable
	private DateTime lastContactsSyncDate;

	@Nullable
	private DateTime lastChatsSyncDate;

	@Nullable
	private DateTime lastUserIconsSyncDate;

	private AccountSyncDataImpl() {
	}

	AccountSyncDataImpl(@Nullable DateTime lastContactsSyncDate,
						@Nullable DateTime lastChatsSyncDate,
						@Nullable DateTime lastUserIconsSyncDate) {
		this.lastContactsSyncDate = lastContactsSyncDate;
		this.lastChatsSyncDate = lastChatsSyncDate;
		this.lastUserIconsSyncDate = lastUserIconsSyncDate;
	}

	@Nonnull
	static AccountSyncDataImpl newInstance(@Nullable DateTime lastContactsSyncDate,
										   @Nullable DateTime lastChatsSyncDate,
										   @Nullable DateTime lastUserIconsSyncDate) {
		return new AccountSyncDataImpl(lastContactsSyncDate, lastChatsSyncDate, lastUserIconsSyncDate);
	}

	@Nonnull
	static AccountSyncDataImpl copyOf(@Nonnull AccountSyncData accountSyncData) {
		return new AccountSyncDataImpl(accountSyncData.getLastContactsSyncDate(), accountSyncData.getLastChatsSyncDate(), accountSyncData.getLastUserIconsSyncData());
	}

	@Nonnull
	static AccountSyncDataImpl newInstance(@Nullable String lastContactsSyncDateString,
										   @Nullable String lastChatsSyncDateString,
										   @Nullable String lastUserIconsSyncDateString) {
		final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();
		final DateTime lastContactsSyncDate = lastContactsSyncDateString == null ? null : dateTimeFormatter.parseDateTime(lastContactsSyncDateString);
		final DateTime lastChatsSyncDate = lastChatsSyncDateString == null ? null : dateTimeFormatter.parseDateTime(lastChatsSyncDateString);
		final DateTime lastUserIconsSyncDate = lastUserIconsSyncDateString == null ? null : dateTimeFormatter.parseDateTime(lastUserIconsSyncDateString);
		return AccountSyncDataImpl.newInstance(lastContactsSyncDate, lastChatsSyncDate, lastUserIconsSyncDate);
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
	public MutableAccountSyncData updateChatsSyncDate() {
		final AccountSyncDataImpl clone = this.clone();
		clone.lastChatsSyncDate = DateTime.now();
		return clone;
	}

	@Nonnull
	@Override
	public MutableAccountSyncData updateContactsSyncDate() {
		final AccountSyncDataImpl clone = this.clone();
		clone.lastContactsSyncDate = DateTime.now();
		return clone;
	}

	@Nonnull
	@Override
	public MutableAccountSyncData updateUserIconsSyncDate() {
		final AccountSyncDataImpl clone = this.clone();
		clone.lastUserIconsSyncDate = DateTime.now();
		return clone;
	}

	@Override
	public boolean isFirstSyncDone() {
		return getLastContactsSyncDate() != null;
	}

	@Nonnull
	@Override
	public AccountSyncDataImpl clone() {
		// dates are immutable => can leave links as is
		return (AccountSyncDataImpl) super.clone();
	}
}
