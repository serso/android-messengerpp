package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/9/13
 * Time: 2:45 PM
 */
public enum RealmEventType {

    /**
     * Fired when realm is created
     */
    created,

    /**
     * Fired when realm is changed
     */
    changed,

    /**
     * Fires when realm is removed
     */
    removed,

    /**
     * Fires when realm connection should be stopped for realm
     */
    stop;

    @Nonnull
    RealmEvent newEvent(@Nonnull Realm realm, @Nullable Object data) {
        assert data == null;
        return new RealmEvent(realm, this, null);
    }
}
