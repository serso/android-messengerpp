package org.solovyev.android.messenger.realms.sms;

import com.google.inject.Inject;
import org.solovyev.android.messenger.realms.BaseRealmConfigurationFragment;
import org.solovyev.android.messenger.realms.RealmConfiguration;
import org.solovyev.android.messenger.realms.RealmDef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 5/27/13
 * Time: 9:08 PM
 */
public final class SmsRealmConfigurationFragment extends BaseRealmConfigurationFragment<SmsRealm> {

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

	public SmsRealmConfigurationFragment() {
		super(R.layout.mpp_realm_sms_conf);
	}

	@Nullable
	@Override
	protected RealmConfiguration validateData() {
		return new SmsRealmConfiguration();
	}

	@Nonnull
	@Override
	public RealmDef getRealmDef() {
		return realmDef;
	}
}
