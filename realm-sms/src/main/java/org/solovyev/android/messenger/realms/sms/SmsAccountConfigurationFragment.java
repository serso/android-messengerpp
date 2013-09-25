package org.solovyev.android.messenger.realms.sms;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment;
import org.solovyev.android.messenger.realms.Realm;

import com.google.inject.Inject;

/**
 * User: serso
 * Date: 5/27/13
 * Time: 9:08 PM
 */
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

		if (!isNewRealm()) {
			final SmsAccount account = getEditedRealm();
			final SmsAccountConfiguration configuration = account.getConfiguration();

			stopFurtherProcessingCheckbox.setChecked(configuration.isStopFurtherProcessing());
		}
	}

	@Nullable
	@Override
	protected AccountConfiguration validateData() {
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
