package org.solovyev.android.messenger.accounts;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.gson.Gson;
import com.google.inject.Singleton;
import org.solovyev.android.db.*;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.Converter;
import org.solovyev.common.security.Cipherer;
import org.solovyev.common.security.CiphererException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import javax.inject.Inject;
import java.util.Collection;

import static java.util.Collections.emptyList;
import static org.solovyev.android.db.AndroidDbUtils.doDbQuery;
import static org.solovyev.android.messenger.App.getExceptionHandler;
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

	@Nonnull
	private Dao<Account> dao;

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
		dao = new SqliteDao<Account>("accounts", "id", new AccountDaoMapper(secret), getContext(), getSqliteOpenHelper());
	}

	@Override
	public long create(@Nonnull Account account) throws AccountRuntimeException {
		return dao.create(account);
	}

	@Nullable
	@Override
	public Account read(@Nonnull String accountId) {
		return dao.read(accountId);
	}

	@Override
	public void deleteById(@Nonnull String accountId) {
		dao.deleteById(accountId);
	}

	@Nonnull
	@Override
	public Collection<Account> readAll() {
		try {
			return dao.readAll();
		} catch (AccountRuntimeException e) {
			getExceptionHandler().handleException(e);
			return emptyList();
		}
	}

	@Nonnull
	@Override
	public Collection<String> readAllIds() {
		return dao.readAllIds();
	}

	@Override
	public void deleteAll() {
		dao.deleteAll();
	}

	@Override
	public long update(@Nonnull Account account) throws AccountRuntimeException {
		return dao.update(account);
	}

	@Override
	public void delete(@Nonnull Account account) {
		dao.delete(account);
	}

	@Nonnull
	@Override
	public Collection<Account> loadAccountsInState(@Nonnull AccountState state) {
		try {
			return doDbQuery(getSqliteOpenHelper(), new LoadAccount(getContext(), state, getSqliteOpenHelper()));
		} catch (AccountRuntimeException e) {
			getExceptionHandler().handleException(e);
			return emptyList();
		}
	}

    /*
	**********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

	private static class AccountDaoMapper implements SqliteDaoEntityMapper<Account> {

		@Nullable
		private final SecretKey secret;

		@Nonnull
		private final Converter<Cursor, Account> cursorMapper;

		private AccountDaoMapper(@Nullable SecretKey secret) {
			this.secret = secret;
			this.cursorMapper = new AccountMapper(secret);
		}

		@Nonnull
		@Override
		public ContentValues toContentValues(@Nonnull Account account) throws AccountRuntimeException {
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

		@Nonnull
		@Override
		public Converter<Cursor, Account> getCursorMapper() {
			return cursorMapper;
		}
	}

	private class LoadAccount extends AbstractDbQuery<Collection<Account>> {

		@Nonnull
		private final AccountState state;

		protected LoadAccount(@Nonnull Context context,
							  @Nonnull AccountState state,
							  @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
			this.state = state;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.query("accounts", null, "state = ?", new String[]{state.name()}, null, null, null);
		}

		@Nonnull
		@Override
		public Collection<Account> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<Account>(new AccountMapper(secret)).convert(cursor);
		}
	}
}
