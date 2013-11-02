package org.solovyev.android.messenger;

import android.app.Application;
import android.content.Context;
import android.test.AndroidTestCase;
import android.test.ApplicationTestCase;
import android.test.InstrumentationTestCase;
import com.google.inject.Inject;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.realms.RealmService;
import roboguice.RoboGuice;

import javax.annotation.Nonnull;

import static roboguice.RoboGuice.getBaseApplicationInjector;

public abstract class BaseInstrumentationTest extends InstrumentationTestCase {

	@Nonnull
	@Inject
	private Application application;

	@Nonnull
	@Inject
	private AccountService accountService;

	@Nonnull
	@Inject
	private RealmService realmService;

	public void setUp() throws Exception {
		super.setUp();
		Thread.sleep(100);
		application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
		getBaseApplicationInjector(application).injectMembers(this);
		App.init(application);
	}

	@Nonnull
	public Application getApplication() {
		return application;
	}

	@Nonnull
	public Context getContext() {
		return application;
	}

	@Nonnull
	public AccountService getAccountService() {
		return accountService;
	}

	@Nonnull
	public RealmService getRealmService() {
		return realmService;
	}
}
