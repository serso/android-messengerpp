package org.solovyev.android.messenger.realms;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.view.AbstractMessengerListItem;
import org.solovyev.android.messenger.view.ViewAwareTag;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RealmListItem extends AbstractMessengerListItem<Realm> {

    @Nonnull
    private static final String TAG_PREFIX = "realm_list_item_view_";

    /*
    **********************************************************************
    *
    *                           VIEWS
    *
    **********************************************************************
    */


    public RealmListItem(@Nonnull Realm realm) {
        super(TAG_PREFIX, R.layout.mpp_list_item_realm, realm);
    }

    @Nullable
    @Override
    public OnClickAction getOnClickAction() {
        return new OnClickAction() {
            @Override
            public void onClick(@Nonnull Context context, @Nonnull ListAdapter<? extends ListItem> adapter, @Nonnull ListView listView) {
                final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
                eventManager.fire(RealmGuiEventType.newRealmClickedEvent(getRealm()));
            }
        };
    }

    @Nonnull
    private Realm getRealm() {
        return getData();
    }

    @Nullable
    @Override
    public OnClickAction getOnLongClickAction() {
        return null;
    }

    public void onRealmChangedEvent(@Nonnull RealmChangedEvent event, @Nonnull Context context) {
        final Realm realm = getRealm();
        if ( event.getRealm().equals(realm)) {
            setData(event.getRealm());
        }
    }

    @Nonnull
    @Override
    protected String getDataId(@Nonnull Realm realm) {
        return realm.getId();
    }

    @Override
    protected void fillView(@Nonnull Realm realm, @Nonnull Context context, @Nonnull ViewAwareTag viewTag) {
        final ImageView realmIconImageView = viewTag.getViewById(R.id.mpp_realm_icon_imageview);
        final TextView realmNameTextView = viewTag.getViewById(R.id.mpp_realm_name_textview);

        final Drawable realmIcon = context.getResources().getDrawable(realm.getRealmDef().getIconResId());
        realmIconImageView.setImageDrawable(realmIcon);

        realmNameTextView.setText(realm.getDisplayName(context));
    }
}
