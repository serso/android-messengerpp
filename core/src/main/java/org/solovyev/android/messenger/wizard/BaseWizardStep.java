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

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ScrollView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

public class BaseWizardStep extends RoboSherlockFragment {

	@Inject
	@Nonnull
	private EventManager eventManager;

	@Nonnull
	protected View inflateView(int layoutResId) {
		final View view = LayoutInflater.from(getActivity()).inflate(layoutResId, null);

		final ScrollView scrollView = new ScrollView(getActivity());
		scrollView.addView(view);
		return scrollView;
	}

	@Nonnull
	protected EventManager getEventManager() {
		return eventManager;
	}
}
