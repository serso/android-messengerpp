package org.solovyev.android.messenger.users;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.MessengerConfigurationImpl;
import org.solovyev.android.messenger.R;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.messages.MessengerMessagesActivity;
import org.solovyev.android.view.ViewFromLayoutBuilder;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:04 PM
 */
public class FriendListItem implements ListItem<View>, UserEventListener, Comparable<FriendListItem> {

    @NotNull
    private static final String TAG_PREFIX = "friend_list_item_view_";

    @NotNull
    private User user;

    @NotNull
    private User friend;

    public FriendListItem(@NotNull User user, @NotNull User friend) {
        this.user = user;
        this.friend = friend;
    }

    @Override
    public OnClickAction getOnClickAction() {
        return new OnClickAction() {
            @Override
            public void onClick(@NotNull final Context context, @NotNull ListAdapter<ListItem<? extends View>> adapter, @NotNull ListView listView) {
                if (context instanceof Activity) {
                    new AsyncTask<Void, Void, Chat>() {

                        @Override
                        protected Chat doInBackground(Void... params) {
                            return MessengerConfigurationImpl.getInstance().getServiceLocator().getUserService().getPrivateChat(user.getId(), friend.getId(), context);
                        }

                        @Override
                        protected void onPostExecute(@NotNull Chat chat) {
                            super.onPostExecute(chat);
                            MessengerMessagesActivity.startActivity((Activity) context, chat);
                        }

                    }.execute();
                }
            }
        };
    }

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
        final ViewGroup view = (ViewGroup) ViewFromLayoutBuilder.newInstance(R.layout.msg_list_item_friend).build(context);
        fillView(view, context);
        return view;
    }

    @NotNull
    private String createTag() {
        return TAG_PREFIX + friend.getId();
    }

    private void fillView(@NotNull final ViewGroup view, @NotNull Context context) {
        final String tag = createTag();

        if (!tag.equals(view.getTag())) {
            view.setTag(tag);

            final ImageView friendIcon = (ImageView) view.findViewById(R.id.friend_icon);

            MessengerConfigurationImpl.getInstance().getServiceLocator().getUserService().setUserIcon(friendIcon, friend, context);

            final TextView friendName = (TextView) view.findViewById(R.id.friend_name);
            friendName.setText(friend.getDisplayName());

            final TextView friendOnline = (TextView) view.findViewById(R.id.friend_online);
            if (friend.isOnline()) {
                friendOnline.setText("·");
            } else {
                friendOnline.setText("");
            }
        }
    }

    @Override
    public void onUserEvent(@NotNull User eventUser, @NotNull UserEventType userEventType, @Nullable Object data) {
        if (userEventType == UserEventType.changed) {
            if (eventUser.equals(user)) {
                user = eventUser;
            }

            if (eventUser.equals(friend)) {
                friend = eventUser;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendListItem)) return false;

        FriendListItem that = (FriendListItem) o;

        if (!friend.equals(that.friend)) return false;
        if (!user.equals(that.user)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + friend.hashCode();
        return result;
    }

    @Override
    public String toString() {
        // NOTE: this code is used inside the ArrayAdapter for filtering
        return friend.getDisplayName();
    }

    @Override
    public int compareTo(@NotNull FriendListItem another) {
        return this.toString().compareTo(another.toString());
    }
}
