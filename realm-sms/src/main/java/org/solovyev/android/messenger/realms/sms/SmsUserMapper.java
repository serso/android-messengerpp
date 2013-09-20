package org.solovyev.android.messenger.realms.sms;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.Properties;
import org.solovyev.common.Converter;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 5/27/13
 * Time: 9:46 PM
 */
public final class SmsUserMapper implements Converter<Cursor, User> {

	static final String[] COLUMNS = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_ID};

	@Nonnull
	private final SmsAccount realm;

	@Nonnull
	private final ContentResolver contentResolver;

	public SmsUserMapper(@Nonnull SmsAccount realm, @Nonnull ContentResolver contentResolver) {
		this.realm = realm;
		this.contentResolver = contentResolver;
	}

	@Nonnull
	@Override
	public User convert(@Nonnull Cursor cursor) {
		final String userId = cursor.getString(0);

		final StringBuilder phoneNumbers = new StringBuilder();
		String defaultPhoneNumber = null;

		// get the phone number
		final String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
		final String[] selectionArgs = {userId};
		final Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection, selectionArgs, null);
		try {
			while (phoneCursor.moveToNext()) {
				final String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				if (!Strings.isEmpty(phoneNumber)) {
					if (phoneNumbers.length() != 0) {
						// append before second, third, etc
						phoneNumbers.append(User.PROPERTY_PHONES_SEPARATOR);
						// more than one phone number, we don't know which number is default
						defaultPhoneNumber = null;
					} else {
						// first time
						defaultPhoneNumber = phoneNumber;
					}
					phoneNumbers.append(phoneNumber);
				}
			}
		} finally {
			phoneCursor.close();
		}

		final List<AProperty> properties = new ArrayList<AProperty>();
		Users.tryParseNameProperties(properties, cursor.getString(1));
		if (phoneNumbers.length() > 0) {
			properties.add(Properties.newProperty(User.PROPERTY_PHONES, phoneNumbers.toString()));
		}
		if (defaultPhoneNumber != null) {
			properties.add(Properties.newProperty(User.PROPERTY_PHONE, defaultPhoneNumber));
		}
		return Users.newUser(realm.getId(), userId, Users.newNeverSyncedUserSyncData(), properties);
	}
}
