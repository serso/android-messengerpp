/*
 * Copyright 2014 serso aka se.solovyev
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

package org.solovyev.android.messenger.about;

import android.app.Application;
import android.content.pm.PackageManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.solovyev.android.Resources;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.solovyev.android.messenger.about.AboutFragmentLineProcessor.APP_VERSION;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class AboutFragmentLineProcessorTest {

	@Nonnull
	private Application application;

	@Nonnull
	private Resources.LineProcessor processor;

	@Before
	public void setUp() throws Exception {
		application = Robolectric.application;
		prepareProcessorForVersion("test");
	}

	private void prepareProcessorForVersion(@Nullable String version) throws PackageManager.NameNotFoundException {
		application.getPackageManager().getPackageInfo(application.getPackageName(), 0).versionName = version;
		processor = new AboutFragmentLineProcessor(application);
	}

	@Test
	public void testShouldSubstituteAppVersion() throws Exception {
		assertEquals("Version: test.", processor.process("Version: " + APP_VERSION + "."));
		assertEquals("test", processor.process(APP_VERSION));
		assertEquals("test test test", processor.process(APP_VERSION + " " + APP_VERSION + " " + APP_VERSION));
	}

	@Test
	public void testShouldUseEmptyVersionInCaseOfNull() throws Exception {
		prepareProcessorForVersion(null);
		assertEquals("Version: .", processor.process("Version: " + APP_VERSION + "."));
	}
}
