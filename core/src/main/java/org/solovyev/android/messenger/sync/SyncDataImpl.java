package org.solovyev.android.messenger.sync;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 6:25 PM
 */
public class SyncDataImpl implements SyncData {

    @NotNull
    private final String realmId;

    public SyncDataImpl(@NotNull String realmId) {
        this.realmId = realmId;
    }

    @NotNull
    @Override
    public String getRealmId() {
        return this.realmId;
    }
}
