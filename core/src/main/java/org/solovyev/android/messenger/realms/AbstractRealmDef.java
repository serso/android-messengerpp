package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 1:05 AM
 */
public abstract class AbstractRealmDef implements RealmDef {

    @NotNull
    private final String id;

    private final int nameResId;

    private final int iconResId;


    protected AbstractRealmDef(@NotNull String id,
                               int nameResId,
                               int iconResId) {
        this.id = id;
        this.nameResId = nameResId;
        this.iconResId = iconResId;
    }

    @NotNull
    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int getNameResId() {
        return this.nameResId;
    }

    @Override
    public int getIconResId() {
        return this.iconResId;
    }
}
