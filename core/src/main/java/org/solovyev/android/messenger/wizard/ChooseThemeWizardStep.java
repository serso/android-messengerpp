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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import org.solovyev.android.messenger.MessengerPreferences;
import org.solovyev.android.messenger.MessengerTheme;
import org.solovyev.android.messenger.core.R;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.messenger.App.getPreferences;
import static org.solovyev.android.messenger.App.getTheme;

public class ChooseThemeWizardStep extends BaseWizardStep {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflateView(R.layout.mpp_wizard_step_choose_theme);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final Spinner themesSpinner = (Spinner) root.findViewById(R.id.mpp_wizard_themes_spinner);
		themesSpinner.setAdapter(createAdapter());
		themesSpinner.setSelection(getTheme().ordinal());
		themesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				MessengerPreferences.Gui.theme.putPreference(getPreferences(), themes()[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	@Nonnull
	private ArrayAdapter<String> createAdapter() {
		final List<String> items = new ArrayList<String>();
		for (MessengerTheme theme : themes()) {
			items.add(getString(theme.getNameResId()));
		}
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}

	@Nonnull
	private static MessengerTheme[] themes() {
		return MessengerTheme.values();
	}
}
