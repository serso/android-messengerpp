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

package org.solovyev.android.messenger.realms.sms;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import com.google.inject.Inject;
import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment;
import org.solovyev.android.messenger.realms.Realm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class SmsAccountConfigurationFragment extends BaseAccountConfigurationFragment<SmsAccount> {

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private SmsRealm realm;

	private CheckBox stopFurtherProcessingCheckbox;

	public SmsAccountConfigurationFragment() {
		super(R.layout.mpp_realm_sms_conf);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		stopFurtherProcessingCheckbox = (CheckBox) root.findViewById(R.id.mpp_sms_conf_stop_processing_checkbox);

		if (!isNewAccount()) {
			final SmsAccount account = getEditedAccount();
			final SmsAccountConfiguration configuration = account.getConfiguration();

			stopFurtherProcessingCheckbox.setChecked(configuration.isStopFurtherProcessing());
		}
	}

	@Nullable
	@Override
	public AccountConfiguration validateData() {
		final SmsAccountConfiguration configuration = new SmsAccountConfiguration();
		configuration.setStopFurtherProcessing(stopFurtherProcessingCheckbox.isChecked());
		return configuration;
	}

	@Nonnull
	@Override
	public Realm getRealm() {
		return realm;
	}
}
