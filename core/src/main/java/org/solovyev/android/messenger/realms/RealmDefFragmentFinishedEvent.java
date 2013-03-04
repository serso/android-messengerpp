package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/4/13
 * Time: 10:39 AM
 */
class RealmDefFragmentFinishedEvent {

    @Nonnull
    private final RealmDef realmDef;

    public RealmDefFragmentFinishedEvent(@Nonnull RealmDef realmDef) {
        this.realmDef = realmDef;
    }

    @Nonnull
    public RealmDef getRealmDef() {
        return realmDef;
    }
}
