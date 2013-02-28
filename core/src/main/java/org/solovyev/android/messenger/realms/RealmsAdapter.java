package org.solovyev.android.messenger.realms;

import android.content.Context;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.MessengerListItemAdapter;

import java.util.List;

public class RealmsAdapter extends MessengerListItemAdapter<RealmListItem>  {

    public RealmsAdapter(@NotNull Context context, @NotNull List<? extends RealmListItem> listItems) {
        super(context, listItems);
    }

    /*
    **********************************************************************
    *
    *                           REALM LISTENERS
    *
    **********************************************************************
    */

    public void onRealmEvent(@NotNull RealmEvent e) {
        if ( e instanceof RealmAddedEvent ) {
            final RealmAddedEvent event = (RealmAddedEvent) e;
            final Realm newRealm = event.getRealm();

            addListItem(createListItem(newRealm));
        } else if ( e instanceof RealmChangedEvent ) {
            final RealmChangedEvent event = (RealmChangedEvent) e;
            final Realm changedRealm = event.getRealm();

            final RealmListItem listItem = findInAllElements(changedRealm);
            if (listItem != null) {
                listItem.onRealmChangedEvent(event, getContext());
            }
        } else if ( e instanceof RealmRemovedEvent ) {
            final RealmRemovedEvent event = (RealmRemovedEvent) e;
            final Realm removedRealm = event.getRealm();

            removeListItem(createListItem(removedRealm));
        }
    }

    @Nullable
    protected RealmListItem findInAllElements(@NotNull Realm realm) {
        return Iterables.find(getAllElements(), Predicates.<RealmListItem>equalTo(createListItem(realm)), null);
    }

    @NotNull
    private RealmListItem createListItem(@NotNull Realm realm) {
        return new RealmListItem(realm);
    }

}
