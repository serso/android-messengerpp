package org.solovyev.android.messenger.realms.sms;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import org.solovyev.android.db.ListMapper;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 5/27/13
 * Time: 9:23 PM
 */
final class SmsAccountUserService implements AccountUserService {

	@Nonnull
	private final SmsAccount realm;

	SmsAccountUserService(@Nonnull SmsAccount realm) {
		this.realm = realm;
	}

	@Nullable
	@Override
	public User getUserById(@Nonnull String realmUserId) throws AccountConnectionException {
		final Context context = MessengerApplication.getApp();

		if (!SmsRealm.USER_ID.equals(realmUserId)) {
			final String selection = ContactsContract.Contacts._ID + " = ? and " + ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1";
			final String[] selectionArgs = new String[]{realmUserId};

			final ContentResolver cr = context.getContentResolver();
			final Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, SmsUserMapper.COLUMNS, selection, selectionArgs, null);
			if (!cursor.isAfterLast()) {
				return new SmsUserMapper(realm, cr).convert(cursor);
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
							user = new SmsUserEmailMapper(realm).convert(emailCursor);
						}
					} finally {
						emailCursor.close();
					}

					if (user != null) {
						break;
					}
				}
			}

			return user == null ? realm.getUser() : Users.newUser(realm.newRealmEntity(SmsRealm.USER_ID), Users.newNeverSyncedUserSyncData(), user.getProperties());
		}
	}

	@Nonnull
	@Override
	public List<User> getUserContacts(@Nonnull String realmUserId) throws AccountConnectionException {
		final String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1";
		final ContentResolver cr = MessengerApplication.getApp().getContentResolver();
		final Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, SmsUserMapper.COLUMNS, selection, null, null);
		return new ListMapper<User>(new SmsUserMapper(realm, cr)).convert(cursor);
	}

	@Nonnull
	@Override
	public List<User> checkOnlineUsers(@Nonnull List<User> users) throws AccountConnectionException {
		return Collections.emptyList();
	}
}
