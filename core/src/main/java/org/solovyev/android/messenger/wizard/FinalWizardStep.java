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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.solovyev.android.messenger.core.R;

import static android.content.Intent.ACTION_VIEW;
import static org.solovyev.android.messenger.App.*;

public class FinalWizardStep extends BaseWizardStep {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflateView(R.layout.mpp_wizard_step_final);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final View becomeTesterButton = root.findViewById(R.id.mpp_wizard_final_become_tester_button);
		becomeTesterButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showUrl(GOOGLE_PLUS_TESTERS_URL);
			}
		});

		final View translateButton = root.findViewById(R.id.mpp_wizard_final_translate_button);
		translateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showUrl(CROWDIN_URL);
			}
		});

		final View contributeButton = root.findViewById(R.id.mpp_wizard_final_contribute_button);
		contributeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showUrl(GITHUB_URL);
			}
		});
	}

	private void showUrl(String url) {
		startActivity(new Intent(ACTION_VIEW, Uri.parse(url)));
	}
}
