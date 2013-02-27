package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.MessengerListItemAdapter;

import java.util.List;

public class RealmsAdapter extends MessengerListItemAdapter<RealmListItem> {

    public RealmsAdapter(@NotNull Context context, @NotNull List<? extends RealmListItem> listItems) {
        super(context, listItems);
    }
}
