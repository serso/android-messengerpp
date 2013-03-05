package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 8:59 PM
 */
public class RealmDefGuiEvent {

    @Nonnull
    private final RealmDefGuiEventType type;

    @Nonnull
    private final RealmDef realmDef;

    @Nullable
    private final Object data;

    public RealmDefGuiEvent(@Nonnull RealmDefGuiEventType type, @Nonnull RealmDef realmDef, @Nullable Object data) {
        this.type = type;
        this.realmDef = realmDef;
        this.data = data;
    }

    @Nonnull
    public RealmDefGuiEventType getType() {
        return type;
    }

    @Nonnull
    public RealmDef getRealmDef() {
        return realmDef;
    }

    @Nullable
    public Object getData() {
        return data;
    }
}
