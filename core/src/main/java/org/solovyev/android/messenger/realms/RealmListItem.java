package org.solovyev.android.messenger.realms;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

public class RealmListItem implements ListItem {

    @NotNull
    private static final String TAG_PREFIX = "realm_list_item_view_";

    @NotNull
    private Realm realm;

    /*
    **********************************************************************
    *
    *                           VIEWS
    *
    **********************************************************************
    */

    @NotNull
    private ImageView realmIconImageView;

    @NotNull
    private TextView realmNameTextView;


    public RealmListItem(@NotNull Realm realm) {
        this.realm = realm;
    }

    @Nullable
    @Override
    public OnClickAction getOnClickAction() {
        return new OnClickAction() {
            @Override
            public void onClick(@NotNull Context context, @NotNull ListAdapter<? extends ListItem> adapter, @NotNull ListView listView) {
                final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
                eventManager.fire(new MessengerRealmsFragment.RealmClickedEvent(realm));
            }
        };
    }

    @Nullable
    @Override
    public OnClickAction getOnLongClickAction() {
        return null;
    }

    @NotNull
    @Override
    public View updateView(@NotNull Context context, @NotNull View view) {
        if (String.valueOf(view.getTag()).startsWith(TAG_PREFIX)) {
            fillView((ViewGroup) view, context);
            return view;
        } else {
            return build(context);
        }
    }

    @NotNull
    @Override
    public View build(@NotNull Context context) {
        final ViewGroup view = (ViewGroup) ViewFromLayoutBuilder.newInstance(R.layout.msg_list_item_realm).build(context);
        fillView(view, context);
        return view;
    }

    @NotNull
    private String createTag() {
        return TAG_PREFIX + realm.getId();
    }

    private void fillView(@NotNull final ViewGroup root, @NotNull final Context context) {
        final String tag = createTag();

        if (!tag.equals(root.getTag())) {
            root.setTag(tag);

            realmIconImageView = (ImageView) root.findViewById(R.id.mpp_realm_icon_imageview);
            realmNameTextView = (TextView) root.findViewById(R.id.mpp_realm_name_textview);

            fillRealmDefValues(context);
            fillRealmValues(context);
        }
    }

    private void fillRealmDefValues(@NotNull Context context) {
        final Drawable realmIcon = context.getResources().getDrawable(realm.getRealmDef().getIconResId());
        realmIconImageView.setImageDrawable(realmIcon);
    }

    private void fillRealmValues(@NotNull Context context) {
        realmNameTextView.setText(realm.getDisplayName(context));
    }

    public void onRealmChangedEvent(@NotNull RealmChangedEvent event, @NotNull Context context) {
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
