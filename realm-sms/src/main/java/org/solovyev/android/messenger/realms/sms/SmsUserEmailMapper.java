package org.solovyev.android.messenger.realms.sms;

import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.Properties;
import org.solovyev.common.Converter;

/**
 * User: serso
 * Date: 5/27/13
 * Time: 9:46 PM
 */
public final class SmsUserEmailMapper implements Converter<Cursor, User> {

	static final String[] COLUMNS = new String[]{
			ContactsContract.CommonDataKinds.Email.CONTACT_ID,
			ContactsContract.CommonDataKinds.Email.DATA,
			ContactsContract.Contacts.DISPLAY_NAME};

	private SmsAccount realm;

	public SmsUserEmailMapper(@Nonnull SmsAccount realm) {
		this.realm = realm;
	}

	@Nonnull
	@Override
	public User convert(@Nonnull Cursor cursor) {
		final String userId = cursor.getString(0);
		final String email = cursor.getString(1);

		final List<AProperty> properties = new ArrayList<AProperty>();
		Users.tryParseNameProperties(properties, cursor.getString(2));
		properties.add(Properties.newProperty(User.PROPERTY_EMAIL, email));
		return Users.newUser(realm.getId(), userId, Users.newNeverSyncedUserSyncData(), properties);
	}
}
