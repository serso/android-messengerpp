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

package org.solovyev.android.messenger.wizard;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.wizard.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

@Singleton
public class MessengerWizards implements Wizards {

	@Nonnull
	public static final String FIRST_TIME_WIZARD = "first-time";

	@Nonnull
	private Context context;

	@Inject
	public MessengerWizards(@Nonnull Application context) {
		this.context = context;
	}

	@Nonnull
	@Override
	public Class<? extends Activity> getActivityClassName() {
		return WizardActivity.class;
	}

	@Nonnull
	@Override
	public Wizard getWizard(@Nullable String name) throws IllegalArgumentException {
		final List<WizardStep> steps;
		if (FIRST_TIME_WIZARD.equals(name)) {
			steps = Arrays.<WizardStep>asList(MessengerWizardStep.values());
		} else {
			steps = null;
		}

		if (steps != null) {
			return new BaseWizard(FIRST_TIME_WIZARD, context, new ListWizardFlow(steps));
		} else {
			throw new IllegalArgumentException("Wizard " + name + " is not supported");
		}
	}
}
