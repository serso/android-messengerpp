package org.solovyev.android.messenger.realms.sms;

import com.google.inject.Inject;
import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment;
import org.solovyev.android.messenger.realms.RealmDef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
	private SmsRealmDef realmDef;

	public SmsAccountConfigurationFragment() {
		super(R.layout.mpp_realm_sms_conf);
	}

	@Nullable
	@Override
	protected AccountConfiguration validateData() {
		return new SmsAccountConfiguration();
	}

	@Nonnull
	@Override
	public RealmDef getRealmDef() {
		return realmDef;
	}
}
