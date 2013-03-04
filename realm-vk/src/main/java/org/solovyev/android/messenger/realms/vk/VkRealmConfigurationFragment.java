package org.solovyev.android.messenger.realms.vk;

import com.google.inject.Inject;
import org.solovyev.android.messenger.realms.BaseRealmConfigurationFragment;
import org.solovyev.android.messenger.realms.RealmDef;

import javax.annotation.Nonnull;

public class VkRealmConfigurationFragment extends BaseRealmConfigurationFragment<VkRealm> {

    @Inject
    @Nonnull
    private VkRealmDef realmDef;

    public VkRealmConfigurationFragment() {
        super(0);
    }

    @Nonnull
    @Override
    public RealmDef getRealmDef() {
        return realmDef;
    }
}
