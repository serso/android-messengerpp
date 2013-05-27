package org.solovyev.android.messenger.realms.sms;

import android.database.Cursor;
import android.provider.ContactsContract;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.Converter;

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

	private SmsRealm realm;

	public SmsUserMapper(@Nonnull SmsRealm realm) {
		this.realm = realm;
	}

	@Nonnull
	@Override
	public User convert(@Nonnull Cursor cursor) {
		final int userId = cursor.getInt(0);

		final List<AProperty> properties = new ArrayList<AProperty>();
		Users.tryParseNameProperties(properties, cursor.getString(1));
		return Users.newUser(realm.getId(), String.valueOf(userId), Users.newNeverSyncedUserSyncData(), properties);
	}
}
