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

public final class RealmListItem extends AbstractMessengerListItem<Realm> {

    @Nonnull
    private static final String TAG_PREFIX = "realm_list_item_";

    /*
    **********************************************************************
    *
    *                           VIEWS
    *
    **********************************************************************
    */


    public RealmListItem(@Nonnull Realm realm) {
        super(TAG_PREFIX, realm, R.layout.mpp_list_item_realm);
    }

    @Nullable
    @Override
    public OnClickAction getOnClickAction() {
        return new OnClickAction() {
            @Override
            public void onClick(@Nonnull Context context, @Nonnull ListAdapter<? extends ListItem> adapter, @Nonnull ListView listView) {
                final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
                eventManager.fire(RealmGuiEventType.newRealmViewRequestedEvent(getRealm()));
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

    public void onRealmChangedEvent(@Nonnull Realm eventRealm, @Nonnull Context context) {
        final Realm realm = getRealm();
        if (realm.equals(eventRealm)) {
            setData(eventRealm);
        }
    }

    @Nonnull
    @Override
    protected String getDisplayName(@Nonnull Realm realm, @Nonnull Context context) {
        return realm.getUser().getDisplayName();
    }

    @Override
    protected void fillView(@Nonnull Realm realm, @Nonnull Context context, @Nonnull ViewAwareTag viewTag) {
        final ImageView realmIconImageView = viewTag.getViewById(R.id.mpp_realm_icon_imageview);

        final Drawable realmIcon = context.getResources().getDrawable(realm.getRealmDef().getIconResId());
        realmIconImageView.setImageDrawable(realmIcon);

        final TextView realmUserNameTextView = viewTag.getViewById(R.id.mpp_realm_user_name_textview);
        realmUserNameTextView.setText(getDisplayName());

        final TextView realmNameTextView = viewTag.getViewById(R.id.mpp_realm_name_textview);
        realmNameTextView.setText(realm.getDisplayName(context));
    }
}
