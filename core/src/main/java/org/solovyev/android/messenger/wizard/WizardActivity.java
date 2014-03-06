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
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import org.solovyev.android.messenger.ActivityUi;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.UiEvent;
import org.solovyev.android.messenger.UiEventListener;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.wizard.*;

import javax.annotation.Nonnull;

public class WizardActivity extends RoboSherlockFragmentActivity implements FinishWizardConfirmationDialog.Listener, WizardsAware {

	@Nonnull
	private final ActivityUi ui = new WizardActivityUi();

	private final WizardUi<WizardActivity> wizardUi = new WizardUi<WizardActivity>(this, this, R.layout.mpp_wizard);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ui.onBeforeCreate();
		super.onCreate(savedInstanceState);
		ui.onCreate(savedInstanceState);
		wizardUi.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		ui.onResume();
		ui.getListeners().add(UiEvent.class, new UiEventListener(this));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		wizardUi.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		wizardUi.onPause();
		ui.onPause();
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		wizardUi.onBackPressed();
	}

	@Nonnull
	@Override
	public Wizards getWizards() {
		return App.getWizards();
	}

	@Override
	public void finishWizardAbruptly() {
		wizardUi.finishWizardAbruptly();
	}

	private final class WizardActivityUi extends ActivityUi {

		public WizardActivityUi() {
			super(WizardActivity.this, true);
		}

		@Override
		protected void restartActivity() {
			// if we want to start from the last opened step we need to update step in the intent
			final WizardStep step = wizardUi.getStep();
			if (step != null) {
				getActivity().getIntent().putExtra("step", step.getName());
			}
			super.restartActivity();
		}
	}
}
