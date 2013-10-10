package org.solovyev.android.messenger.realms.sms;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
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
import static org.solovyev.android.messenger.users.Users.newUser;
import static org.solovyev.android.properties.Properties.newProperty;

/**
 * User: serso
 * Date: 5/27/13
 * Time: 9:46 PM
 */
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

		final List<AProperty> properties = new ArrayList<AProperty>();
		Users.tryParseNameProperties(properties, cursor.getString(1));
		if (phoneNumbers.size() == 1) {
			properties.add(newProperty(User.PROPERTY_PHONE, getFirst(phoneNumbers, null)));
		} else {
			final StringBuilder sb = new StringBuilder();
			for (String phoneNumber : phoneNumbers) {
				if(sb.length() != 0) {
					sb.append(User.PROPERTY_PHONES_SEPARATOR);
				}
				sb.append(phoneNumber);
			}
			properties.add(newProperty(User.PROPERTY_PHONES, sb.toString()));
		}
		return newUser(account.getId(), userId, Users.newNeverSyncedUserSyncData(), properties);
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
				final String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				if (Users.isValidPhoneNumber(phoneNumber)) {
					phoneNumbers.add(phoneNumber);
				}
			}
		} finally {
			phoneCursor.close();
		}
		return phoneNumbers;
	}
}
