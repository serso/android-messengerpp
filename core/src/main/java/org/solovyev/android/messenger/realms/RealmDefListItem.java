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

class RealmDefListItem extends AbstractMessengerListItem<RealmDef> {

    @Nonnull
    private static final String TAG_PREFIX = "realm_def_list_item_";

    RealmDefListItem(@Nonnull RealmDef realmDef) {
        super(TAG_PREFIX, R.layout.mpp_list_item_realm_def, realmDef);
    }

    @Nullable
    @Override
    public OnClickAction getOnClickAction() {
        return new OnClickAction() {
            @Override
            public void onClick(@Nonnull Context context, @Nonnull ListAdapter<? extends ListItem> adapter, @Nonnull ListView listView) {
                final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
                eventManager.fire(RealmDefGuiEventType.newRealmDefClickedEvent(getRealmDef()));
            }
        };
    }

    @Nonnull
    private RealmDef getRealmDef() {
        return getData();
    }

    @Nullable
    @Override
    public OnClickAction getOnLongClickAction() {
        return null;
    }

    @Nonnull
    @Override
    protected String getDisplayName(@Nonnull RealmDef realmDef, @Nonnull Context context) {
        return context.getString(realmDef.getNameResId());
    }

    @Override
    protected void fillView(@Nonnull RealmDef realmDef, @Nonnull Context context, @Nonnull ViewAwareTag viewTag) {
        final ImageView realmDefIconImageView = viewTag.getViewById(R.id.mpp_realm_def_icon_imageview);
        final Drawable configuredRealmIcon = context.getResources().getDrawable(realmDef.getIconResId());
        realmDefIconImageView.setImageDrawable(configuredRealmIcon);

        final TextView realmDefNameTextView = viewTag.getViewById(R.id.mpp_realm_def_name_textview);
        realmDefNameTextView.setText(getDisplayName());
    }
}
