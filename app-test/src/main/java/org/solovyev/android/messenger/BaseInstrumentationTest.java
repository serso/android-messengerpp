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
