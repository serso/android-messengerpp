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
import org.solovyev.android.messenger.accounts.AccountsActivity;
import org.solovyev.android.messenger.core.R;

public class AddAccountsWizardStep extends BaseWizardStep {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflateView(R.layout.mpp_wizard_step_add_accounts);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final View addAccountsButton = view.findViewById(R.id.mpp_wizard_add_accounts_button);
		addAccountsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AccountsActivity.startForNewAccounts(getActivity());
			}
		});
	}
}
