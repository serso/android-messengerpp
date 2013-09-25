package org.solovyev.android.messenger.accounts;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import javax.inject.Inject;

import org.solovyev.android.db.AbstractDbQuery;
import org.solovyev.android.db.AbstractObjectDbExec;
import org.solovyev.android.db.AbstractSQLiteHelper;
import org.solovyev.android.db.AndroidDbUtils;
import org.solovyev.android.db.DbExec;
import org.solovyev.android.db.DeleteAllRowsDbExec;
import org.solovyev.android.db.ListMapper;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.security.Cipherer;
import org.solovyev.common.security.CiphererException;

import com.google.gson.Gson;
import com.google.inject.Singleton;

import static org.solovyev.android.messenger.App.getSecurityService;

@Singleton
public class SqliteAccountDao extends AbstractSQLiteHelper implements AccountDao {

	@Inject
	@Nonnull
	private UserService userService;

	@Inject
	@Nonnull
	private AccountService accountService;

	@Nullable
	private SecretKey secret;

	@Inject
	public SqliteAccountDao(@Nonnull Application context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
		super(context, sqliteOpenHelper);
	}

	SqliteAccountDao(@Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
		super(context, sqliteOpenHelper);
	}

	@Override
	public void init() {
		secret = getSecurityService().getSecretKey();
	}

	@Override
	public void insertAccount(@Nonnull Account account) throws AccountException {
		try {
			AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new InsertAccount(account, secret)));
		} catch (AccountRuntimeException e) {
			throw new AccountException(e);
		}
	}

	@Override
	public void deleteAccount(@Nonnull String accountId) {
		AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new DeleteAccount(accountId)));
	}

	@Nonnull
	@Override
	public Collection<Account> loadAccounts() {
		try {
			return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadAccount(getContext(), null, getSqliteOpenHelper()));
		} catch (AccountRuntimeException e) {
			App.getExceptionHandler().handleException(e);
			return Collections.emptyList();
		}
	}

	@Override
	public void deleteAllAccounts() {
		AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(DeleteAllRowsDbExec.newInstance("accounts")));
	}

	@Override
	public void updateAccount(@Nonnull Account account) throws AccountException {
		try {
			AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new UpdateAccount(account, secret)));
		} catch (AccountRuntimeException e) {
			throw new AccountException(e);
		}
	}

	@Nonnull
	@Override
	public Collection<Account> loadAccountsInState(@Nonnull AccountState state) {
		try {
			return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadAccount(getContext(), state, getSqliteOpenHelper()));
		} catch (AccountRuntimeException e) {
			App.getExceptionHandler().handleException(e);
			return Collections.emptyList();
		}
	}

    /*
	**********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

	private static class InsertAccount extends AbstractObjectDbExec<Account> {

		@Nullable
		private final SecretKey secret;

		public InsertAccount(@Nonnull Account account, @Nullable SecretKey secret) {
			super(account);
			this.secret = secret;
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final Account account = getNotNullObject();

			final ContentValues values = toContentValues(account, secret);

			return db.insert("accounts", null, values);
		}
	}

	private static class UpdateAccount extends AbstractObjectDbExec<Account> {

		@Nullable
		private final SecretKey secret;

		public UpdateAccount(@Nonnull Account account, @Nullable SecretKey secret) {
			super(account);
			this.secret = secret;
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final Account account = getNotNullObject();

			final ContentValues values = toContentValues(account, secret);

			return db.update("accounts", values, "id = ?", new String[]{account.getId()});
		}
	}

	@Nonnull
	private static ContentValues toContentValues(@Nonnull Account account, @Nullable SecretKey secret) throws AccountRuntimeException {
		final ContentValues values = new ContentValues();

		values.put("id", account.getId());
		values.put("realm_id", account.getRealm().getId());
		values.put("user_id", account.getUser().getEntity().getEntityId());

		final AccountConfiguration configuration;

		try {
			final Cipherer<AccountConfiguration, AccountConfiguration> cipherer = account.getRealm().getCipherer();
			if (cipherer != null && secret != null) {
				configuration = cipherer.encrypt(secret, account.getConfiguration());
			} else {
				configuration = account.getConfiguration();
			}
			values.put("configuration", new Gson().toJson(configuration));
		} catch (CiphererException e) {
			throw new AccountRuntimeException(account.getId(), e);
		}

		values.put("state", account.getState().name());

		return values;
	}

	private class LoadAccount extends AbstractDbQuery<Collection<Account>> {

		@Nullable
		private final AccountState state;

		protected LoadAccount(@Nonnull Context context, @Nullable AccountState state, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
			this.state = state;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			if (state == null) {
				return db.query("accounts", null, null, null, null, null, null);
			} else {
				return db.query("accounts", null, "state = ?", new String[]{state.name()}, null, null, null);
			}
		}

		@Nonnull
		@Override
		public Collection<Account> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<Account>(new AccountMapper(secret)).convert(cursor);
		}
	}

	private static class DeleteAccount extends AbstractObjectDbExec<String> {

		public DeleteAccount(@Nonnull String realmId) {
			super(realmId);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final String realmId = getNotNullObject();

			return db.delete("accounts", "id = ?", new String[]{realmId});
		}
	}

}
