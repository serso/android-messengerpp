package org.solovyev.android.messenger.realms;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RealmListItem implements ListItem {

    @Nonnull
    private static final String TAG_PREFIX = "realm_list_item_view_";

    @Nonnull
    private Realm realm;

    /*
    **********************************************************************
    *
    *                           VIEWS
    *
    **********************************************************************
    */

    @Nonnull
    private ImageView realmIconImageView;

    @Nonnull
    private TextView realmNameTextView;


    public RealmListItem(@Nonnull Realm realm) {
        this.realm = realm;
    }

    @Nullable
    @Override
    public OnClickAction getOnClickAction() {
        return new OnClickAction() {
            @Override
            public void onClick(@Nonnull Context context, @Nonnull ListAdapter<? extends ListItem> adapter, @Nonnull ListView listView) {
                final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
                eventManager.fire(RealmGuiEventType.newRealmClickedEvent(realm));
            }
        };
    }

    @Nullable
    @Override
    public OnClickAction getOnLongClickAction() {
        return null;
    }

    @Nonnull
    @Override
    public View updateView(@Nonnull Context context, @Nonnull View view) {
        if (String.valueOf(view.getTag()).startsWith(TAG_PREFIX)) {
            fillView((ViewGroup) view, context);
            return view;
        } else {
            return build(context);
        }
    }

    @Nonnull
    @Override
    public View build(@Nonnull Context context) {
        final ViewGroup view = (ViewGroup) ViewFromLayoutBuilder.newInstance(R.layout.msg_list_item_realm).build(context);
        fillView(view, context);
        return view;
    }

    @Nonnull
    private String createTag() {
        return TAG_PREFIX + realm.getId();
    }

    private void fillView(@Nonnull final ViewGroup root, @Nonnull final Context context) {
        final String tag = createTag();

        if (!tag.equals(root.getTag())) {
            root.setTag(tag);

            realmIconImageView = (ImageView) root.findViewById(R.id.mpp_realm_icon_imageview);
            realmNameTextView = (TextView) root.findViewById(R.id.mpp_realm_name_textview);

            fillRealmDefValues(context);
            fillRealmValues(context);
        }
    }

    private void fillRealmDefValues(@Nonnull Context context) {
        final Drawable realmIcon = context.getResources().getDrawable(realm.getRealmDef().getIconResId());
        realmIconImageView.setImageDrawable(realmIcon);
    }

    private void fillRealmValues(@Nonnull Context context) {
        realmNameTextView.setText(realm.getDisplayName(context));
    }

    public void onRealmChangedEvent(@Nonnull RealmChangedEvent event, @Nonnull Context context) {
        if ( event.getRealm().equals(realm)) {
            this.realm = event.getRealm();
            fillRealmValues(context);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RealmListItem)) return false;

        RealmListItem that = (RealmListItem) o;

        if (!realm.equals(that.realm)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return realm.hashCode();
    }
}
