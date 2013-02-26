package org.solovyev.android.messenger;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.ConfiguredRealmListItem;

import java.util.List;

public class MessengerConfiguredRealmListItemAdapter extends AbstractMessengerListItemAdapter<ConfiguredRealmListItem> {

    public MessengerConfiguredRealmListItemAdapter(@NotNull Context context,
                                                   @NotNull List<? extends ConfiguredRealmListItem> listItems) {
        super(context, listItems);
    }
}
