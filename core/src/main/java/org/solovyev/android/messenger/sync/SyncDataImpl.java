package org.solovyev.android.messenger.sync;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 6:25 PM
 */
public class SyncDataImpl implements SyncData {

    @Nonnull
    private final String realmId;

    public SyncDataImpl(@Nonnull String realmId) {
        this.realmId = realmId;
    }

    @Nonnull
    @Override
    public String getRealmId() {
        return this.realmId;
    }
}
