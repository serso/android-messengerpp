package org.solovyev.android.messenger.chats;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.list.ListItemOnClickData;
import org.solovyev.android.list.SimpleMenuOnClick;
import org.solovyev.android.menu.LabeledMenuItem;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.R;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.view.ViewFromLayoutBuilder;

import java.util.Arrays;

/**
 * User: serso
 * Date: 6/10/12
 * Time: 11:25 PM
 */
public class MessageListItem implements ListItem, ChatEventListener {

    @NotNull
    private static final String LEFT_VIEW_TAG = "left";

    @NotNull
    private static final String RIGHT_VIEW_TAG = "right";

    @NotNull
    private User user;

    @NotNull
    private Chat chat;

    @NotNull
    private ChatMessage message;

    @NotNull
    private final String leftTag;

    @NotNull
    private final String rightTag;

    // todo serso: add listener interfaces

    public MessageListItem(@NotNull User user, @NotNull Chat chat, @NotNull ChatMessage message) {
        this.user = user;
        this.chat = chat;
        this.message = message;
        this.leftTag = LEFT_VIEW_TAG + "_" + message.getId();
        this.rightTag = RIGHT_VIEW_TAG + "_" + message.getId();
    }

    @Override
    public OnClickAction getOnClickAction() {
        return new SimpleMenuOnClick<MessageListItem>(Arrays.<LabeledMenuItem<ListItemOnClickData<MessageListItem>>>asList(MenuItems.values()), this);
    }

    @Override
    public OnClickAction getOnLongClickAction() {
        return null;
    }

    @NotNull
    @Override
    public View updateView(@NotNull Context context, @NotNull View view) {

        final String viewTag = view.getTag() == null ? null : String.valueOf(view.getTag());

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean userMessage = isUserMessage();
        if (userMessage == isUserMessagesToTheRight(preferences)) {
            if (getRightTag().equals(viewTag)) {
                // this is the view for this list item => just return
                return view;
            } else if (viewTag != null && viewTag.startsWith(RIGHT_VIEW_TAG)) {
                // this is the view for this TYPE of list item => refill it with values
                view.setTag(getRightTag());
                fillView(context, view, preferences, userMessage);
                return view;
            }
        } else {
            if (getLeftTag().equals(viewTag)) {
                // this is the view for this list item => just return
                return view;
            } else if (viewTag != null && viewTag.startsWith(LEFT_VIEW_TAG)) {
                // this is the view for this TYPE of list item => refill it with values
                view.setTag(getLeftTag());
                fillView(context, view, preferences, userMessage);
                return view;
            }
        }

        return build(context);
    }

    @NotNull
    @Override
    public View build(@NotNull Context context) {

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        final View root;
        boolean userMessage = isUserMessage();
        if (userMessage == isUserMessagesToTheRight(preferences)) {
            root = ViewFromLayoutBuilder.newInstance(R.layout.msg_list_item_message_right).build(context);
            root.setTag(getRightTag());
        } else {
            root = ViewFromLayoutBuilder.newInstance(R.layout.msg_list_item_message_left).build(context);
            root.setTag(getLeftTag());
        }

        fillView(context, root, preferences, userMessage);

        return root;
    }

    @NotNull
    private String getLeftTag() {
        return leftTag;
    }

    @NotNull
    private String getRightTag() {
        return rightTag;
    }

    private boolean isUserMessagesToTheRight(@NotNull SharedPreferences preferences) {
        return MessengerApplication.Preferences.Gui.Chat.userMessagesPosition.getPreference(preferences) == MessengerApplication.Preferences.Gui.Chat.UserIconPosition.right;
    }

    private boolean isUserMessage() {
        return user.equals(message.getAuthor());
    }

    private void fillView(@NotNull Context context, @NotNull View root, @NotNull SharedPreferences preferences, boolean userMessage) {
        final TextView messageText = (TextView) root.findViewById(R.id.message_body);
        messageText.setText(Html.fromHtml(message.getBody()));

        final TextView messageDate = (TextView) root.findViewById(R.id.message_date);
        messageDate.setText(ChatListItem.getMessageTime(message));

        final ImageView messageIcon = (ImageView) root.findViewById(R.id.message_icon);
        if (userMessage) {
            fillMessageIcon(context, messageIcon, MessengerApplication.Preferences.Gui.Chat.showUserIcon.getPreference(preferences));
        } else {
            if (chat.isPrivate()) {
                fillMessageIcon(context, messageIcon, MessengerApplication.Preferences.Gui.Chat.showContactIconInPrivateChat.getPreference(preferences));
            } else {
                fillMessageIcon(context, messageIcon, MessengerApplication.Preferences.Gui.Chat.showContactIconInChat.getPreference(preferences));
            }
        }
    }

    private void fillMessageIcon(@NotNull Context context, @NotNull ImageView messageIcon, @NotNull Boolean show) {
        if (show) {
            messageIcon.setVisibility(View.VISIBLE);
            MessengerApplication.getServiceLocator().getChatMessageService().setMessageIcon(messageIcon, message, chat, user, context);
        } else {
            messageIcon.setImageResource(R.drawable.empty_icon);
            messageIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageListItem)) return false;

        MessageListItem that = (MessageListItem) o;

        if (!message.equals(that.message)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return message.hashCode();
    }

    @Override
    public void onChatEvent(@NotNull Chat eventChat, @NotNull ChatEventType chatEventType, @Nullable Object data) {
        if (ChatEventType.message_changed.isEvent(chatEventType, eventChat, chat)) {
            if (message.equals(data)) {
                message = (ChatMessage) data;
            }
        }
    }

    public static final class Comparator implements java.util.Comparator<MessageListItem> {

        @NotNull
        private static final Comparator instance = new Comparator();

        private Comparator() {
        }

        @NotNull
        public static Comparator getInstance() {
            return instance;
        }

        @Override
        public int compare(@NotNull MessageListItem lhs, @NotNull MessageListItem rhs) {
               return lhs.message.getSendDate().compareTo(rhs.message.getSendDate());
        }
    }

    private static enum MenuItems implements LabeledMenuItem<ListItemOnClickData<MessageListItem>> {

        copy(R.string.c_copy) {
            @Override
            public void onClick(@NotNull ListItemOnClickData<MessageListItem> data, @NotNull Context context) {
                final android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Activity.CLIPBOARD_SERVICE);

                final MessageListItem messageListItem = data.getDataObject();
                clipboard.setText(messageListItem.message.getBody());

                Toast.makeText(context, context.getString(R.string.c_message_copied), Toast.LENGTH_SHORT).show();
            }
        };

        private int captionResId;

        private MenuItems(int captionResId) {
            this.captionResId = captionResId;
        }

        @NotNull
        @Override
        public String getCaption(@NotNull Context context) {
            return context.getString(captionResId);
        }
    }
}
