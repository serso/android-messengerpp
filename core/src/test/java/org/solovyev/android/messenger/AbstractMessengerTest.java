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
import com.google.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import javax.annotation.Nonnull;

@RunWith(RobolectricTestRunner.class)
public abstract class AbstractMessengerTest {

	@Nonnull
	@Inject
	private Application application = Robolectric.application;

	@Nonnull
	private AbstractTestModule module;

	@Before
	public void setUp() throws Exception {
		module = newModule(application);
		module.setUp(this, module);
		App.init(application);
		populateDatabase();
	}

	protected abstract void populateDatabase() throws Exception;

	protected abstract AbstractTestModule newModule(@Nonnull Application application);

	@After
	public void tearDown() throws Exception {
		module.tearDown();
	}

	@Nonnull
	public Application getApplication() {
		return application;
	}
}
