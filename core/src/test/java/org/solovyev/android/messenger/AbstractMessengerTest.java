package org.solovyev.android.messenger;

import android.app.Application;
import com.google.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 2/27/13
 * Time: 10:16 PM
 */
@RunWith(RobolectricTestRunner.class)
public abstract class AbstractMessengerTest {

	@Nonnull
	@Inject
	private Application application = Robolectric.application;

	@Nonnull
	private AbstractTestMessengerModule module;

	@Before
	public void setUp() throws Exception {
		module = newModule(application);
		module.setUp(this, module);
		App.init(application);
		populateDatabase();
	}

	protected abstract void populateDatabase() throws Exception;

	protected abstract AbstractTestMessengerModule newModule(@Nonnull Application application);

	@After
	public void tearDown() throws Exception {
		module.tearDown();
	}

	@Nonnull
	public Application getApplication() {
		return application;
	}
}
