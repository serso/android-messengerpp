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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.wizard.WizardStep;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum MessengerWizardStep implements WizardStep {

	welcome(WelcomeWizardStep.class, R.string.mpp_wizard_welcome),
	choose_theme(ChooseThemeWizardStep.class, R.string.mpp_wizard_choose_theme),
	add_accounts(AddAccountsWizardStep.class, R.string.mpp_wizard_add_accounts),
	last(FinalWizardStep.class, R.string.mpp_wizard_final);

	@Nonnull
	private final Class<? extends Fragment> fragmentClass;

	private final int titleResId;
	private final int nextButtonTitleResId;

	MessengerWizardStep(@Nonnull Class<? extends Fragment> fragmentClass, int titleResId) {
		this(fragmentClass, titleResId, R.string.acl_wizard_next);
	}

	MessengerWizardStep(@Nonnull Class<? extends Fragment> fragmentClass, int titleResId, int nextButtonTitleResId) {
		this.fragmentClass = fragmentClass;
		this.titleResId = titleResId;
		this.nextButtonTitleResId = nextButtonTitleResId;
	}

	@Nonnull
	@Override
	public String getFragmentTag() {
		return name();
	}

	@Override
	@Nonnull
	public Class<? extends Fragment> getFragmentClass() {
		return fragmentClass;
	}

	@Override
	public int getTitleResId() {
		return titleResId;
	}

	@Override
	public int getNextButtonTitleResId() {
		return nextButtonTitleResId;
	}

	@Override
	public boolean onNext(@Nonnull Fragment fragment) {
		return true;
	}

	@Override
	public boolean onPrev(@Nonnull Fragment fragment) {
		return true;
	}

	@Override
	@Nullable
	public Bundle getFragmentArgs() {
		return null;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	@Nonnull
	public String getName() {
		return name();
	}
}
