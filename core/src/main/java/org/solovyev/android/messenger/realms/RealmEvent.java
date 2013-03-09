package org.solovyev.android.messenger.realms;

import org.solovyev.common.listeners.JEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class RealmEvent implements JEvent {

    @Nonnull
    private Realm realm;

    @Nonnull
    private RealmEventType type;

    @Nullable
    private Object data;

    RealmEvent(@Nonnull Realm realm, @Nonnull RealmEventType type, @Nullable Object data) {
        this.realm = realm;
        this.type = type;
        this.data = data;
    }

    @Nonnull
    public Realm getRealm() {
        return realm;
    }

    @Nonnull
    public RealmEventType getType() {
        return type;
    }

    @Nullable
    public Object getData() {
        return data;
    }
}
