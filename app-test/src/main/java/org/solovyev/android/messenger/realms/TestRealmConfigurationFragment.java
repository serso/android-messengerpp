package org.solovyev.android.messenger.realms;

import com.google.inject.Inject;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 2/27/13
 * Time: 9:15 PM
 */
public class TestRealmConfigurationFragment extends BaseRealmConfigurationFragment {

    @Inject
    @Nonnull
    private TestRealmDef realmDef;

    public TestRealmConfigurationFragment() {
        super(0);
    }

    @Nonnull
    @Override
    public RealmDef getRealmDef() {
        return realmDef;
    }
}
