package org.solovyev.android.messenger.realms;

import android.content.Context;
import android.content.Intent;
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
import org.solovyev.android.messenger.R;
import org.solovyev.android.view.ViewFromLayoutBuilder;

public class RealmDefListItem implements ListItem {

    @NotNull
    private static final String TAG_PREFIX = "realm_def_list_item_view_";

    @NotNull
    private RealmDef realmDef;

    public RealmDefListItem(@NotNull RealmDef realmDef) {
        this.realmDef = realmDef;
    }

    @Nullable
    @Override
    public OnClickAction getOnClickAction() {
        return new OnClickAction() {
            @Override
            public void onClick(@NotNull Context context, @NotNull ListAdapter<? extends ListItem> adapter, @NotNull ListView listView) {
                context.startActivity(new Intent(context, realmDef.getConfigurationActivityClass()));
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
        return TAG_PREFIX + realmDef.getId();
    }

    private void fillView(@NotNull final ViewGroup view, @NotNull final Context context) {
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
