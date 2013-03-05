package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 12:48 PM
 */
public class RealmGuiEvent {

    @Nonnull
    private final RealmGuiEventType type;

    @Nonnull
    private final Realm realm;

    @Nullable
    private final Object data;

    public RealmGuiEvent(@Nonnull RealmGuiEventType type, @Nonnull Realm realm, @Nullable Object data) {
        this.type = type;
        this.realm = realm;
        this.data = data;
    }

    @Nonnull
    public RealmGuiEventType getType() {
        return type;
    }

    @Nonnull
    public Realm getRealm() {
        return realm;
    }

    @Nullable
    public Object getData() {
        return data;
    }
}
