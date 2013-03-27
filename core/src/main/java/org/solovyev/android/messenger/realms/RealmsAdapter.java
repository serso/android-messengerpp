package org.solovyev.android.messenger.realms;

import android.content.Context;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.solovyev.android.messenger.MessengerListItemAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class RealmsAdapter extends MessengerListItemAdapter<RealmListItem>  {

    public RealmsAdapter(@Nonnull Context context, @Nonnull List<? extends RealmListItem> listItems) {
        super(context, listItems);
    }

    /*
    **********************************************************************
    *
    *                           REALM LISTENERS
    *
    **********************************************************************
    */

    public void onRealmEvent(@Nonnull RealmEvent realmEvent) {
        final Realm realm = realmEvent.getRealm();
        switch (realmEvent.getType()) {
            case created:
                addListItem(createListItem(realm));
                break;
            case changed:
                final RealmListItem listItem = findInAllElements(realm);
                if (listItem != null) {
                    listItem.onRealmChangedEvent(realm);
                }
                break;
            case state_changed:
                switch (realm.getState()) {
                    case enabled:
                    case disabled_by_user:
                        final RealmListItem realmListItem = findInAllElements(realm);
                        if ( realmListItem != null ) {
                            realmListItem.onRealmChangedEvent(realm);
                        }
                        break;
                    case removed:
                        removeListItem(createListItem(realm));
                        break;
                }
                break;
        }
    }

    @Nullable
    protected RealmListItem findInAllElements(@Nonnull Realm realm) {
        return Iterables.find(getAllElements(), Predicates.<RealmListItem>equalTo(createListItem(realm)), null);
    }

    @Nonnull
    private RealmListItem createListItem(@Nonnull Realm realm) {
        return new RealmListItem(realm);
    }
}
