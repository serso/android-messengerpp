package org.solovyev.android.messenger.realms;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.MessengerRealmConfigurationActivity;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ViewFromLayoutBuilder;

public class RealmDefListItem implements ListItem {

    @Nonnull
    private static final String TAG_PREFIX = "realm_def_list_item_view_";

    @Nonnull
    private RealmDef realmDef;

    public RealmDefListItem(@Nonnull RealmDef realmDef) {
        this.realmDef = realmDef;
    }

    @Nullable
    @Override
    public OnClickAction getOnClickAction() {
        return new OnClickAction() {
            @Override
            public void onClick(@Nonnull Context context, @Nonnull ListAdapter<? extends ListItem> adapter, @Nonnull ListView listView) {
                MessengerRealmConfigurationActivity.startForNewRealm(context, realmDef);
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
        return TAG_PREFIX + realmDef.getId();
    }

    private void fillView(@Nonnull final ViewGroup view, @Nonnull final Context context) {
        final String tag = createTag();

        if (!tag.equals(view.getTag())) {
            view.setTag(tag);

            final ImageView configuredRealmIconImageView = (ImageView) view.findViewById(R.id.mpp_realm_icon_imageview);
            final Drawable configuredRealmIcon = context.getResources().getDrawable(realmDef.getIconResId());
            configuredRealmIconImageView.setImageDrawable(configuredRealmIcon);

            final TextView configuredRealmNameTextView = (TextView) view.findViewById(R.id.mpp_realm_name_textview);
            configuredRealmNameTextView.setText(realmDef.getNameResId());
        }
    }
}
