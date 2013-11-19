package org.solovyev.android.messenger.realms;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountDao;
import org.solovyev.android.messenger.accounts.AccountRuntimeException;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.users.UserService;

public class UserSavingAccountDao implements AccountDao {

	@Nonnull
	private final AccountDao dao;

	@Nonnull
	private final UserService userService;

	public UserSavingAccountDao(@Nonnull UserService userService, @Nonnull AccountDao dao) {
		this.userService = userService;
		this.dao = dao;
	}

	@Override
	public void init() {
		dao.init();
	}

	@Override
	public long create(@Nonnull Account account) throws AccountRuntimeException {
		final long result = dao.create(account);
		if (result >= 0) {
			userService.saveAccountUser(account.getUser());
		}
		return result;
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
	@Nonnull
	public Collection<Account> loadAccountsInState(@Nonnull AccountState state) {
		return dao.loadAccountsInState(state);
	}

	@Override
	@Nullable
	public Account read(@Nonnull String id) {
		return dao.read(id);
	}

	@Override
	@Nonnull
	public Collection<Account> readAll() {
		return dao.readAll();
	}

	@Override
	@Nonnull
	public Collection<String> readAllIds() {
		return dao.readAllIds();
	}

	@Override
	public void delete(@Nonnull Account entity) {
		dao.delete(entity);
	}

	@Override
	public void deleteById(@Nonnull String id) {
		dao.deleteById(id);
	}
}
