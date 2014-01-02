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
import org.solovyev.android.Resources;

import javax.annotation.Nonnull;

import static org.solovyev.common.text.Strings.getNotEmpty;

class AboutFragmentLineProcessor implements Resources.LineProcessor {

	static final String APP_VERSION = "${app.version}";

	@Nonnull
	private final String version;

	AboutFragmentLineProcessor(@Nonnull Application application) {
		version = getAppVersion(application);
	}

	@Nonnull
	private static String getAppVersion(@Nonnull Application application) {
		String version = null;

		try {
			version = application.getPackageManager().getPackageInfo(application.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			// impossible
		}

		return getNotEmpty(version, "");
	}

	@Nonnull
	@Override
	public String process(@Nonnull String line) {
		return line.replace(APP_VERSION, version);
	}
}
