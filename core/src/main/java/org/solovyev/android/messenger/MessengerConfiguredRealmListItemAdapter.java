package org.solovyev.android.messenger;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.ConfiguredRealmListItem;
import org.solovyev.android.messenger.users.User;

import java.util.List;

public class MessengerConfiguredRealmListItemAdapter extends AbstractMessengerListItemAdapter<ConfiguredRealmListItem> {

    public MessengerConfiguredRealmListItemAdapter(@NotNull Context context,
                                                   @NotNull List<? extends ConfiguredRealmListItem> listItems,
                                                   @NotNull User user) {
        super(context, listItems, user);
    }
}
