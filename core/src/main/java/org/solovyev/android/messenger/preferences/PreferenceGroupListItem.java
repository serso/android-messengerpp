package org.solovyev.android.messenger.preferences;

import android.content.Context;
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

/**
 * User: serso
 * Date: 3/17/13
 * Time: 5:57 PM
 */
public final class PreferenceGroupListItem extends AbstractMessengerListItem<PreferenceGroup> {

    @Nonnull
    private static final String TAG_PREFIX = "preference_group_list_item_";

    public PreferenceGroupListItem(@Nonnull PreferenceGroup preferenceGroup) {
        super(TAG_PREFIX, R.layout.mpp_list_item_preference, preferenceGroup);
    }

    @Nonnull
    @Override
    protected CharSequence getDisplayName(@Nonnull PreferenceGroup preferenceGroup, @Nonnull Context context) {
        return preferenceGroup.getName();
    }

    @Override
    protected void fillView(@Nonnull PreferenceGroup preferenceGroup, @Nonnull Context context, @Nonnull ViewAwareTag viewTag) {
        final ImageView preferenceIconImageView = viewTag.getViewById(R.id.mpp_li_preference_icon_imageview);

        if (preferenceGroup.hasIcon()) {
            preferenceIconImageView.setImageDrawable(context.getResources().getDrawable(preferenceGroup.getIconResId()));
        } else {
            preferenceIconImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.empty_icon));
        }

        final TextView preferenceNameTextView = viewTag.getViewById(R.id.mpp_li_preference_name_textview);
        preferenceNameTextView.setText(getDisplayName());
    }

    @Nullable
    @Override
    public OnClickAction getOnClickAction() {
        return new OnClickAction() {
            @Override
            public void onClick(@Nonnull Context context, @Nonnull ListAdapter<? extends ListItem> adapter, @Nonnull ListView listView) {
                final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
                eventManager.fire(PreferenceGuiEventType.preference_group_clicked.newEvent(getData()));
            }
        };
    }

    @Nullable
    @Override
    public OnClickAction getOnLongClickAction() {
        return null;
    }
}
