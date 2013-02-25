package org.solovyev.android.messenger.realms;

import android.content.Context;
import android.view.View;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.list.ListItem;

public class ConfiguredRealmListItem implements ListItem {

    @NotNull
    private ConfiguredRealm configuredRealm;

    public ConfiguredRealmListItem(@NotNull ConfiguredRealm configuredRealm) {
        this.configuredRealm = configuredRealm;
    }

    @Nullable
    @Override
    public OnClickAction getOnClickAction() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nullable
    @Override
    public OnClickAction getOnLongClickAction() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public View updateView(@NotNull Context context, @NotNull View view) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public View build(@NotNull Context context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
