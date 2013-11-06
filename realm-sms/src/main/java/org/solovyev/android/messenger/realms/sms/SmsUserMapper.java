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

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.PhoneNumber;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.Converter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.getFirst;
import static org.solovyev.android.messenger.entities.Entities.newEntity;
import static org.solovyev.android.messenger.users.PhoneNumber.newPhoneNumber;
import static org.solovyev.android.messenger.users.Users.newNeverSyncedUserSyncData;
import static org.solovyev.android.messenger.users.Users.newUser;
import static org.solovyev.common.text.Strings.isEmpty;

public final class SmsUserMapper implements Converter<Cursor, User> {

	static final String[] COLUMNS = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_ID};

	@Nonnull
	private final SmsAccount account;

	@Nonnull
	private final ContentResolver contentResolver;

	public SmsUserMapper(@Nonnull SmsAccount account, @Nonnull ContentResolver contentResolver) {
		this.account = account;
		this.contentResolver = contentResolver;
	}

	@Nonnull
	@Override
	public User convert(@Nonnull Cursor cursor) {
		final String userId = cursor.getString(0);

		final Set<String> phoneNumbers = getPhoneNumbers(userId);

		final List<AProperty> propertiesList = new ArrayList<AProperty>();
		Users.tryParseNameProperties(propertiesList, cursor.getString(1));
		final MutableUser user = newUser(newEntity(account.getId(), userId), newNeverSyncedUserSyncData(), propertiesList);

		if (phoneNumbers.size() == 1) {
			user.getProperties().setProperty(User.PROPERTY_PHONE, getFirst(phoneNumbers, null));
		} else {
			final StringBuilder sb = new StringBuilder();
			for (String phoneNumber : phoneNumbers) {
				if (sb.length() != 0) {
					sb.append(User.PROPERTY_PHONES_SEPARATOR);
				}
				sb.append(phoneNumber);
			}
			user.getProperties().setProperty(User.PROPERTY_PHONES, sb.toString());
		}

		if (!account.isCompositeUserDefined(user)) {
			final User oldUser = App.getUserService().getUserById(user.getEntity(), false);
			final String phone = oldUser.getPropertyValueByName(User.PROPERTY_PHONE);
			if (!isEmpty(phone)) {
				user.getProperties().setProperty(User.PROPERTY_PHONE, phone);
			}
		}

		return user;
	}

	@Nonnull
	private Set<String> getPhoneNumbers(@Nonnull String userId) {
		final Set<String> phoneNumbers = new HashSet<String>();

		// get the phone number
		final String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
		final String[] selectionArgs = {userId};
		final Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection, selectionArgs, null);
		try {
			while (phoneCursor.moveToNext()) {
				final PhoneNumber phoneNumber = newPhoneNumber(phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
				if (phoneNumber.isValid()) {
					phoneNumbers.add(phoneNumber.getNumber());
				}
			}
		} finally {
			phoneCursor.close();
		}
		return phoneNumbers;
	}
}
