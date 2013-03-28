package org.solovyev.android.messenger.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.core.R;
import org.solovyev.common.msg.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class NotificationListItem implements ListItem {

    @Nonnull
    private final Message notification;

    public NotificationListItem(@Nonnull Message notification) {
        this.notification = notification;
    }

    @Nullable
    @Override
    public OnClickAction getOnClickAction() {
        return null;
    }

    @Nullable
    @Override
    public OnClickAction getOnLongClickAction() {
        return null;
    }

    @Nonnull
    @Override
    public View updateView(@Nonnull Context context, @Nonnull View view) {
        return build(context);
    }

    @Nonnull
    @Override
    public View build(@Nonnull Context context) {
        final LayoutInflater li = LayoutInflater.from(context);
        final View root = li.inflate(R.layout.mpp_list_item_notification, null);

        root.findViewById(R.id.mpp_li_notification_icon_imageview).setVisibility(View.GONE);

        final TextView notificationTextTextView = (TextView) root.findViewById(R.id.mpp_li_notification_text_textview);
        notificationTextTextView.setText(notification.getLocalizedMessage());

        final Button notificationRemoveButton = (Button) root.findViewById(R.id.mpp_li_notification_remove_button);
        notificationRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessengerApplication.getServiceLocator().getNotificationService().removeNotification(notification);
            }
        });

        return root;
    }
}
