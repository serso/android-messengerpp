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

package org.solovyev.android.messenger.realms.sms;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import org.solovyev.android.db.ListMapper;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.solovyev.android.messenger.App.getApplication;

final class SmsAccountUserService implements AccountUserService {

	@Nonnull
	private final SmsAccount account;

	SmsAccountUserService(@Nonnull SmsAccount account) {
		this.account = account;
	}

	@Nullable
	@Override
	public User getUserById(@Nonnull String accountUserId) throws AccountConnectionException {
		final Context context = getApplication();

		if (!SmsRealm.USER_ID.equals(accountUserId)) {
			final String selection = ContactsContract.Contacts._ID + " = ? and " + ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1";
			final String[] selectionArgs = new String[]{accountUserId};

			final ContentResolver cr = context.getContentResolver();
			final Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, SmsUserMapper.COLUMNS, selection, selectionArgs, null);
			if (!cursor.isAfterLast()) {
				return new SmsUserMapper(account, cr).convert(cursor);
			} else {
				return null;
			}
		} else {
			final AccountManager manager = AccountManager.get(context);

			Account[] accounts = manager.getAccountsByType("com.google");
			if (accounts == null || accounts.length == 0) {
				accounts = manager.getAccounts();
			}

			User user = null;

			for (Account account : accounts) {
				if (!Strings.isEmpty(account.name)) {
					final ContentResolver cr = context.getContentResolver();
					final Cursor emailCursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, SmsUserEmailMapper.COLUMNS, ContactsContract.CommonDataKinds.Email.DATA + " = ?", new String[]{account.name}, null);
					try {
						if (emailCursor.moveToNext()) {
							user = new SmsUserEmailMapper(this.account).convert(emailCursor);
						}
					} finally {
						emailCursor.close();
					}

					if (user != null) {
						break;
					}
				}
			}

			return user == null ? account.getUser() : Users.newUser(account.newEntity(SmsRealm.USER_ID), user.getPropertiesCollection());
		}
	}

	@Nonnull
	@Override
	public List<User> getUserContacts() throws AccountConnectionException {
		final String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1";
		final ContentResolver cr = getApplication().getContentResolver();
		final Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, SmsUserMapper.COLUMNS, selection, null, null);
		return new ListMapper<User>(new SmsUserMapper(account, cr)).convert(cursor);
	}

	@Nonnull
	@Override
	public List<User> getOnlineUsers() throws AccountConnectionException {
		// users always offline => do not need update
		return emptyList();
	}

	@Nonnull
	@Override
	public User saveUser(@Nonnull User user) {
		return user;
	}
}
